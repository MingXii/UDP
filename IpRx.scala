package workshop.udp_master
import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

case class IpRxGenerics(
    ipWidth: Int, //UDP数据位宽
    cntWidth: Int,
    desIp: Int,
    byteNum: Int
)

case class IpRx(ipGenerics: IpRxGenerics) extends Component {
  val io = new Bundle {
    val ipRxDataIn = slave Stream (Fragment(Bits(ipGenerics.ipWidth bits)))
    val ipRxDataOut = master Stream (Fragment(Bits(ipGenerics.ipWidth bits)))
  }

  io.ipRxDataIn.ready := False
  io.ipRxDataOut.valid := False
  io.ipRxDataOut.fragment := 0
  io.ipRxDataOut.last := False

  val cnt = Reg(UInt(ipGenerics.cntWidth bits)) init (0)
  val ip_head_byte_num = Reg(Bits(6 bits)) init (0)
  val des_ip = Reg(Bits(32 bits)) init (0)

  val fsm = new StateMachine {
    val idle: State = new State with EntryPoint {
      onEntry {
        ip_head_byte_num := 0
        des_ip := 0
      }
      whenIsActive {
        when(io.ipRxDataIn.valid) {
          io.ipRxDataIn.ready := True
          cnt := cnt + 1
          when(cnt === 0) {
            ip_head_byte_num := (io.ipRxDataIn.fragment(3 downto 0) << 2)
          }.elsewhen(cnt >= 16 && cnt <= 18) {
            des_ip := des_ip(23 downto 0) ## io.ipRxDataIn.fragment
          } elsewhen (cnt === 19) {
            des_ip := des_ip(23 downto 0) ## io.ipRxDataIn.fragment
            when(
              (des_ip(
                23 downto 0
              ) ## io.ipRxDataIn.fragment) === ipGenerics.desIp
            ) {
              when(cnt === (ip_head_byte_num.asUInt - 1)) {
                goto(ip2udp)
                cnt := 0
              }.otherwise {
                goto(rx_end)
                cnt := 0
              }
            }
          }
        }
      }
    }

    val ip2udp: State = new State {
      whenIsActive {
        when(io.ipRxDataIn.valid) {
          io.ipRxDataOut << io.ipRxDataIn
        }.otherwise {
          goto(rx_end)
        }
      }
    }

    val rx_end: State = new State {
      whenIsActive {
        when(~io.ipRxDataIn.valid) {
          goto(idle)
        }
      }
    }
  }
}
