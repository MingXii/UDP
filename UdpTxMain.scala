package workshop.udp_master

import spinal.core.{SpinalConfig, SpinalVhdl}

object UdpTxMain{
  def main(args: Array[String]) {
    SpinalConfig(targetDirectory = "rtl").generateVerilog(UdpTx(
      udpGenerics = UdpTxGenerics(
        udpWidth = 32,//UDP数据位宽
        cntWidth = 6,
        srcPort = 37984,
        byteNum =16
      )
    )
    )
  }
}