package workshop.udp_master

import spinal.core.{SpinalConfig, SpinalVhdl}

object IpRxMain{
  def main(args: Array[String]) {
    SpinalConfig(targetDirectory = "rtl").generateVerilog(IpRx(
      ipGenerics = IpRxGenerics(
        ipWidth = 8,//UDP数据位宽
        cntWidth = 5,
        desIp = 1234,
        byteNum = 16
      )
    )
    )
  }
}