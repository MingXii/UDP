package workshop.udp_master
import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

case class MacRxGenerics(
    macWidth: Int, //UDP数据位宽
    cntWidth: Int,
    preamble: Int, //8bit
    sfd: Int,
    desMac: Long,
    ethType: Int,
    byteNum: Int
)

case class MacRx(macGenerics: MacRxGenerics) extends Component {
  val io = new Bundle {
    val macRxDataIn = slave Stream (Fragment(Bits(macGenerics.macWidth bits)))
    val macRxDataOut = master Stream (Fragment(Bits(macGenerics.macWidth bits)))
  }

  io.macRxDataIn.ready := False
  io.macRxDataOut.valid := False
  io.macRxDataOut.fragment := 0
  io.macRxDataOut.last := False

  val cnt = Reg(UInt(macGenerics.cntWidth bits)) init (0)
  val des_mac = Reg(Bits(48 bits)) init (0)
  val eth_type = Reg(Bits(16 bits)) init (0)

  val fsm = new StateMachine {
    val idle: State = new State with EntryPoint {
      whenIsActive {
        when(
          io.macRxDataIn.valid
        ) {
          io.macRxDataIn.ready := True
          when(io.macRxDataIn.fragment === macGenerics.preamble) {
            goto(st_preamble)
          }
        }
      }
    }

    val st_preamble: State = new State {
      onEntry {
        cnt := 0
      }
      whenIsActive {
        when(io.macRxDataIn.valid) {
          io.macRxDataIn.ready := True
          cnt := cnt + 1
          when(cnt < 6 && io.macRxDataIn.fragment =/= macGenerics.preamble) {
            goto(rx_end)
          }.elsewhen(cnt === 6) {
            cnt := 0
            when(io.macRxDataIn.fragment === macGenerics.sfd) {
              goto(eth_head)
            }.otherwise {
              goto(rx_end)
            }
          }
        }
      }
    }

    val eth_head: State = new State {
      onEntry {
        des_mac := 0
        eth_type := 0
      }
      whenIsActive {
        when(io.macRxDataIn.valid) {
          io.macRxDataIn.ready := True
          cnt := cnt + 1
          when(cnt < 6) {
            des_mac := des_mac(39 downto 0) ## io.macRxDataIn.fragment
          }.elsewhen(cnt === 12) {
            eth_type(15 downto 8) := io.macRxDataIn.fragment
          }.elsewhen(cnt === 13) {
            eth_type(7 downto 0) := io.macRxDataIn.fragment
            cnt := 0
            when(
              (des_mac === macGenerics.desMac || des_mac === 0xffffffffffffL) && ((eth_type(
                15 downto 8
              ) ## io.macRxDataIn.fragment) === macGenerics.ethType)
            ) {
              goto(mac2ip)
            }.otherwise {
              goto(rx_end)
            }
          }
        }
      }
    }

    val mac2ip: State = new State {
      whenIsActive {
        when(io.macRxDataIn.valid) {
          io.macRxDataOut << io.macRxDataIn
        }.otherwise {
          goto(rx_end)
        }
      }
    }

    val rx_end: State = new State {
      whenIsActive {
        when(~io.macRxDataIn.valid) {
          goto(idle)
        }
      }
    }
  }
}
