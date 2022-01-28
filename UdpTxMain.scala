package workshop.udp_master

import spinal.core.{SpinalConfig, SpinalVhdl}

object UdpTxMain{
  def main(args: Array[String]) {
    SpinalConfig(targetDirectory = "rtl").generateVerilog(UdpTx(
      udpGenerics = UdpTxGenerics(
        udp_width = 32,//UDP数据位宽
        cnt_width = 6,
        src_port = 37984,
        byte_num =16
      )
    )
    )
  }
}