package workshop.udp_master
import spinal.core.{Bits, _}
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

case class UdpTxGenerics(udpWidth: Int,//UDP数据位宽
                         cntWidth: Int,
                         srcPort : Int,
                         byteNum: Int)

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
    val macTxdata = slave Stream (Fragment(Bits(udpGenerics.udpWidth bits)))
    val byteNum = in UInt(udpGenerics.byteNum bits)
    val dstPort = in Bits(16 bits)
    val gthTxdata = master Stream (Fragment(Bits(8 bits)))
    val txDone = out Bool()
  }

  io.macTxdata.ready := False
  io.gthTxdata.fragment := 0
  io.gthTxdata.last := False
  io.gthTxdata.valid := False
  io.txDone := False

  val crc_data_in = Bits(8 bits)
  val crc_en   = Bool()
  val crc_clr  = Bool()
  val crc_data = Bits(32 bits)
  val crc_next = Bits(8 bits)

  val tx_data_num = Reg(UInt(udpGenerics.byteNum bits)) init(0)
  val total_num = Reg(UInt(udpGenerics.byteNum bits)) init(0)
  val udp_num = Reg(UInt(udpGenerics.byteNum bits)) init(0)
  val cnt = Reg(UInt(udpGenerics.cntWidth bits)) init(0)
  val tx_bit_sel = Reg(UInt(log2Up(udpGenerics.udpWidth/8) bits)) init(0)
  val data_cnt = Reg(UInt(udpGenerics.byteNum bits)) init(0)
  val crc_bit_sel = Reg(UInt(2 bits)) init(0)
  val src_port = Bits(16 bits)

  src_port := udpGenerics.srcPort

  val crc_udp = new Crc32_d8()

  crc_data_in <> crc_udp.io.crc_data_in
  crc_en  <> crc_udp.io.crc_en
  crc_clr <> crc_udp.io.crc_clr
  crc_data <> crc_udp.io.crc_data
  crc_next <> crc_udp.io.crc_next(31 downto 24)

  crc_en := False
  crc_clr := False
  crc_data_in := io.gthTxdata.fragment

  udp_num := io.byteNum + 8

  val fsm = new StateMachine {
    val idle: State = new State with EntryPoint {
      onEntry {
        cnt := 0
      }
      whenIsActive {
        when(io.macTxdata.valid) {
          io.gthTxdata.valid := True
          when(io.gthTxdata.fire){
            cnt := cnt + 1
            crc_en := True
          }
          when(cnt === 0) {
            io.gthTxdata.fragment := src_port(15 downto 8)
          }.elsewhen(cnt === 1) {
            io.gthTxdata.fragment := src_port(7 downto 0)
          }.elsewhen(cnt === 2){
            io.gthTxdata.fragment := io.dstPort(15 downto 8)
          }.elsewhen(cnt === 3) {
            io.gthTxdata.fragment := io.dstPort(7 downto 0)
          }.elsewhen(cnt === 4) {
            io.gthTxdata.fragment := udp_num.asBits(15 downto 8)
          }.elsewhen(cnt === 5) {
            io.gthTxdata.fragment := udp_num.asBits(7 downto 0)
          }.elsewhen(cnt === 6) {
            io.gthTxdata.fragment := 0x00
          }.elsewhen(cnt === 7){
            io.gthTxdata.fragment := 0x00
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
        when(io.macTxdata.valid) {
          StreamFragmentWidthAdapter(input = io.macTxdata,output = io.gthTxdata)
        }
        when(io.gthTxdata.fire){
          when(data_cnt < io.byteNum - 1){
            crc_en := True
            data_cnt := data_cnt + 1
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
        io.gthTxdata.valid := True
        when(io.gthTxdata.ready) {
          crc_bit_sel := crc_bit_sel + 1
          when(crc_bit_sel === 0){
            io.gthTxdata.fragment := ~crc_next
          }.elsewhen(crc_bit_sel === 1){
            io.gthTxdata.fragment := ~crc_data(16 until 24)
          }.elsewhen(crc_bit_sel === 2){
            io.gthTxdata.fragment := ~crc_data(8 until 16)
          }.elsewhen(crc_bit_sel === 3){
            io.gthTxdata.fragment := ~crc_data(0 until 8)
            io.txDone := True
            crc_clr := True
            goto(idle)
          }
        }
      }
    }
  }
}
