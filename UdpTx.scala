/*
package workshop.udp_master
import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

case class UdpTxGenerics(udp_width: Int,//UDP数据位宽
                         cnt_width: Int,
                         src_port : Int,
                         byte_num: Int)

case class UdpTxData(g : UdpTxGenerics) extends Bundle{
  val byte_num  = UInt(g.byte_num bits)
  val data      = Bits(g.udp_width bits)
  val dstPort   = Bits(16 bits)
}

/*case class Crc() extends Bundle{
  val crc_data  = Bits(32 bits)
  val crc_next  = Bits(8 bits)
}*/

case class Crc32_d8() extends BlackBox {
  val crc = new Bundle {
    val data_in = in Bits(8 bits)
    val en   = in Bool()
    val clr  = in Bool()
    val data = out Bits(32 bits)
    val next = out Bits(32 bits)
  }
}

case class UdpTx(udpGenerics:UdpTxGenerics) extends Component {
  val io = new Bundle {
    val mac_txdata = slave Stream (Fragment(UdpTxData(udpGenerics)))
//    val crc = slave Stream(Fragment(Crc()))
    val gth_txdata = master Stream (Fragment(Bits(8 bits)))
    val tx_done = out Bool()
//    val crc_clr = out Bool()
//    val crc_en = out Bool()
  }

  io.mac_txdata.ready := False
  io.gth_txdata.payload := 0
  io.gth_txdata.valid := False
  io.tx_done := False

  val crc_data_in = Bits(8 bits)
  val crc_en   = Bool()
  val crc_clr  = Bool()
  val crc_data = Bits(32 bits)
  val crc_next = Bits(8 bits)

  val tx_data_num = Reg(UInt(udpGenerics.byte_num bits)) init(0)
  val total_num = Reg(UInt(udpGenerics.byte_num bits)) init(0)
  val udp_num = Reg(UInt(udpGenerics.byte_num bits)) init(0)
  val cnt = Reg(UInt(udpGenerics.cnt_width bits)) init(0)
  val tx_bit_sel = Reg(UInt(log2Up(udpGenerics.udp_width/8) bits)) init(0)
  val data_cnt = Reg(UInt(udpGenerics.byte_num bits)) init(0)
  val crc_bit_sel = Reg(UInt(2 bits)) init(0)

  val crc_udp = new Crc32_d8()

  crc_data_in <> crc_udp.crc.data_in
  crc_en  <> crc_udp.crc.en
  crc_clr <> crc_udp.crc.clr
  crc_data <> crc_udp.crc.data
  crc_next <> crc_udp.crc.next(31 downto 24)

  crc_en := False
  crc_clr := False
  crc_data_in := io.gth_txdata.payload

  udp_num := io.mac_txdata.byte_num + 8

  val fsm = new StateMachine {
    val idle: State = new State with EntryPoint {
      onEntry {
        cnt := 0
      }
      whenIsActive {
        when(io.mac_txdata.valid) {
          io.gth_txdata.valid := True
          when(io.gth_txdata.fire){
            cnt := cnt + 1
            crc_en := True
          }
          when(cnt === 0) {
            io.gth_txdata.payload := udpGenerics.src_port
          }.elsewhen(cnt === 0) {
            io.gth_txdata.payload := io.mac_txdata.dstPort
          }.elsewhen(cnt === 1){
            io.gth_txdata.payload := udp_num.asBits
          }.elsewhen(cnt === 2){
            io.gth_txdata.payload := 0x00
            cnt := 0
            goto(tx_data)
          }
        }
      }
    }

    val tx_data: State = new State {
      onEntry {
        tx_bit_sel := 0
        data_cnt := 0
      }
      whenIsActive {
        when(io.mac_txdata.valid) {
          io.gth_txdata.valid := True
          when(io.gth_txdata.ready) {
            io.mac_txdata.ready := True
            tx_bit_sel := tx_bit_sel + 1
            io.gth_txdata.payload := io.mac_txdata.data.subdivideIn(8 bits)(tx_bit_sel)
          }
        }
        when(io.gth_txdata.fire){
          when(data_cnt < io.mac_txdata.byte_num -1){
            crc_en := True
            data_cnt := data_cnt +1
          }.otherwise{
            goto(crc)
          }
        }
      }
    }

    val crc: State = new State {
      onEntry {
        crc_bit_sel := 0
      }
      whenIsActive {
        io.gth_txdata.valid := True
        when(io.gth_txdata.ready) {
          crc_bit_sel := crc_bit_sel + 1
          when(crc_bit_sel === 0){
            io.gth_txdata.payload := ~crc_next
          }.elsewhen(crc_bit_sel === 1){
            io.gth_txdata.payload := ~crc_data(16 until 24)
          }.elsewhen(crc_bit_sel === 2){
            io.gth_txdata.payload := ~crc_data(8 until 16)
          }.elsewhen(crc_bit_sel === 3){
            io.gth_txdata.payload := ~crc_data(0 until 8)
            io.tx_done := True
            crc_clr := True
            goto(idle)
          }
        }
      }
    }
  }
}
*/
package workshop.udp_master
import spinal.core.{Bits, _}
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

case class UdpTxGenerics(udp_width: Int,//UDP数据位宽
                         cnt_width: Int,
                         src_port : Int,
                         byte_num: Int)

case class Crc32_d8() extends BlackBox {
  val io = new Bundle {
    val clk = in Bool()
    val rst = in Bool()
    val crc_data_in = in Bits(8 bits)
    val crc_en   = in Bool()
    val crc_clr  = in Bool()
    val crc_data = out Bits(32 bits)
    val crc_next = out Bits(32 bits)
  }
  noIoPrefix()
  mapClockDomain(clock= io.clk,reset = io.rst)

}

case class UdpTx(udpGenerics:UdpTxGenerics) extends Component {
  val io = new Bundle {
    val mac_txdata = slave Stream (Fragment(Bits(udpGenerics.udp_width bits)))
    val byte_num = in UInt(udpGenerics.byte_num bits)
    val dstPort = in Bits(16 bits)
    val gth_txdata = master Stream (Fragment(Bits(8 bits)))
    val tx_done = out Bool()
  }

  io.mac_txdata.ready := False
  io.gth_txdata.fragment := 0
  io.gth_txdata.last := False
  io.gth_txdata.valid := False
  io.tx_done := False

  val crc_data_in = Bits(8 bits)
  val crc_en   = Bool()
  val crc_clr  = Bool()
  val crc_data = Bits(32 bits)
  val crc_next = Bits(8 bits)

  val tx_data_num = Reg(UInt(udpGenerics.byte_num bits)) init(0)
  val total_num = Reg(UInt(udpGenerics.byte_num bits)) init(0)
  val udp_num = Reg(UInt(udpGenerics.byte_num bits)) init(0)
  val cnt = Reg(UInt(udpGenerics.cnt_width bits)) init(0)
  val tx_bit_sel = Reg(UInt(log2Up(udpGenerics.udp_width/8) bits)) init(0)
  val data_cnt = Reg(UInt(udpGenerics.byte_num bits)) init(0)
  val crc_bit_sel = Reg(UInt(2 bits)) init(0)
  val src_port = Bits(16 bits)

  src_port := udpGenerics.src_port

  val crc_udp = new Crc32_d8()

  crc_data_in <> crc_udp.io.crc_data_in
  crc_en  <> crc_udp.io.crc_en
  crc_clr <> crc_udp.io.crc_clr
  crc_data <> crc_udp.io.crc_data
  crc_next <> crc_udp.io.crc_next(31 downto 24)

  crc_en := False
  crc_clr := False
  crc_data_in := io.gth_txdata.fragment

  udp_num := io.byte_num + 8

  val fsm = new StateMachine {
    val idle: State = new State with EntryPoint {
      onEntry {
        cnt := 0
      }
      whenIsActive {
        when(io.mac_txdata.valid) {
          io.gth_txdata.valid := True
          when(io.gth_txdata.fire){
            cnt := cnt + 1
            crc_en := True
          }
          when(cnt === 0) {
            io.gth_txdata.fragment := src_port(15 downto 8)
          }.elsewhen(cnt === 1) {
            io.gth_txdata.fragment := src_port(7 downto 0)
          }.elsewhen(cnt === 2){
            io.gth_txdata.fragment := io.dstPort(15 downto 8)
          }.elsewhen(cnt === 3) {
            io.gth_txdata.fragment := io.dstPort(7 downto 0)
          }.elsewhen(cnt === 4) {
            io.gth_txdata.fragment := udp_num.asBits(15 downto 8)
          }.elsewhen(cnt === 5) {
            io.gth_txdata.fragment := udp_num.asBits(7 downto 0)
          }.elsewhen(cnt === 6) {
            io.gth_txdata.fragment := 0x00
          }.elsewhen(cnt === 7){
            io.gth_txdata.fragment := 0x00
            cnt := 0
            goto(tx_data)
          }
        }
      }
    }

    val tx_data: State = new State {
      onEntry {
        tx_bit_sel := 0
        data_cnt := 0
      }
      whenIsActive {
        when(io.mac_txdata.valid) {
          StreamFragmentWidthAdapter(input = io.mac_txdata,output = io.gth_txdata)
        }
        when(io.gth_txdata.fire){
          when(data_cnt < io.byte_num -1){
            crc_en := True
            data_cnt := data_cnt +1
          }.otherwise{
            goto(crc)
          }
        }
      }
    }

    val crc: State = new State {
      onEntry {
        crc_bit_sel := 0
      }
      whenIsActive {
        io.gth_txdata.valid := True
        when(io.gth_txdata.ready) {
          crc_bit_sel := crc_bit_sel + 1
          when(crc_bit_sel === 0){
            io.gth_txdata.fragment := ~crc_next
          }.elsewhen(crc_bit_sel === 1){
            io.gth_txdata.fragment := ~crc_data(16 until 24)
          }.elsewhen(crc_bit_sel === 2){
            io.gth_txdata.fragment := ~crc_data(8 until 16)
          }.elsewhen(crc_bit_sel === 3){
            io.gth_txdata.fragment := ~crc_data(0 until 8)
            io.tx_done := True
            crc_clr := True
            goto(idle)
          }
        }
      }
    }
  }
}