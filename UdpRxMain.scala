package workshop.udp_master

import spinal.core.{SpinalConfig, SpinalVhdl}

object UdpRxMain{
  def main(args: Array[String]) {
    SpinalConfig(targetDirectory = "rtl").generateVerilog(UdpRx(
      udpGenerics = UdpRxGenerics(
        udpWidth = 32,//UDP数据位宽
        cntWidth = 6,
        desPort = 37984,
        byteNum =16
      )
    )
    )
  }
}
