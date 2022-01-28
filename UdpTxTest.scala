package workshop.udp_master

import spinal.sim._
import spinal.core.sim._
import workshop.common.WorkshopSimConfig

class udpTxSim(udpGenerics : UdpTxGenerics) extends UdpTx(udpGenerics){
  def init={
    clockDomain.forkStimulus(10)
    io.mac_txdata.valid#=false
    io.gth_txdata.ready#=false
    clockDomain.waitSampling(10)
  }
  def test_1={
    io.mac_txdata.valid#=true
    io.mac_txdata.fragment#=0x1234
    io.byte_num#=50
    io.dstPort#=0x9460
    clockDomain.waitSampling()
    io.mac_txdata.fragment#=0x3478
    clockDomain.waitSampling()
    io.mac_txdata.fragment#=0x9423
    clockDomain.waitSampling()
    io.mac_txdata.fragment#=0x6056
    clockDomain.waitSampling()
    io.mac_txdata.fragment#=0x0075
    clockDomain.waitSampling()
    io.mac_txdata.fragment#=0x6423
    clockDomain.waitSampling()
    io.mac_txdata.fragment#=0x0045
    clockDomain.waitSampling()
    io.mac_txdata.fragment#=0x0089
  }
  def test_2={
    io.mac_txdata.valid#=true
    io.gth_txdata.ready#=true
    io.mac_txdata.fragment#=0x1213
    clockDomain.waitSampling(20)
    io.mac_txdata.fragment#=0x3456
    io.gth_txdata.ready#=false
    clockDomain.waitSampling(20)
    io.gth_txdata.ready#=true
    clockDomain.waitSampling(20)
    io.mac_txdata.fragment#=0x5678
    clockDomain.waitSampling(90)
    io.mac_txdata.valid#=false
  }

}

object UdpTxTests {
  def main(args: Array[String]): Unit = {
    SimConfig
      .withWave
      .addRtl("rtl/Crc32_d8.v")
      .compile(new udpTxSim( udpGenerics = UdpTxGenerics(
        udp_width = 32,//UDP数据位宽
        cnt_width = 6,
        src_port = 37984,
        byte_num =16
      )))
      .doSim{dut=>
        dut.init //接口信号初始化
        SimTimeout(10000)
        dut.test_1
        dut.test_2
        dut.clockDomain.waitSampling(20)
        dut.test_1
        dut.clockDomain.waitSampling(1000)
      }

    // Simulation code here
  }
}