package workshop.udp_master

import spinal.core.{SpinalConfig, SpinalVhdl}

object UdpRxMain{
  def main(args: Array[String]) {
    SpinalConfig(targetDirectory = "rtl").generateVerilog(UdpRx(
      udpGenerics = UdpRxGenerics(
        udp_width = 32,//UDP数据位宽
        cnt_width = 6,
        des_port = 37984,
        byte_num =16
      )
    )
    )
  }
}
