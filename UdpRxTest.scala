package workshop.udp_master

import spinal.sim._
import spinal.core.sim._
import workshop.common.WorkshopSimConfig

class udpRxSim(udpGenerics : UdpRxGenerics) extends UdpRx(udpGenerics){
  def init={
    clockDomain.forkStimulus(10)
    io.mac_rxdata.valid#=false
    io.rx.ready#=false
    clockDomain.waitSampling(10)
  }
  def test_1={
    io.mac_rxdata.valid#=true
    io.mac_rxdata.fragment#=0x12
    clockDomain.waitSampling()
    io.mac_rxdata.fragment#=0x34
    clockDomain.waitSampling()
    io.mac_rxdata.fragment#=0x94
    clockDomain.waitSampling()
    io.mac_rxdata.fragment#=0x60
    clockDomain.waitSampling()
    io.mac_rxdata.fragment#=0x00
    clockDomain.waitSampling()
    io.mac_rxdata.fragment#=0x64
    clockDomain.waitSampling()
    io.mac_rxdata.fragment#=0x00
    clockDomain.waitSampling()
    io.mac_rxdata.fragment#=0x00
  }
  def test_2={
    io.mac_rxdata.valid#=true
    io.rx.ready#=true
    io.mac_rxdata.fragment#=0x12
    clockDomain.waitSampling(20)
    io.mac_rxdata.fragment#=0x34
    io.rx.ready#=false
    clockDomain.waitSampling(20)
    io.rx.ready#=true
    clockDomain.waitSampling(20)
    io.mac_rxdata.fragment#=0x56
    clockDomain.waitSampling(90)
    io.mac_rxdata.valid#=false
  }

}

object UdpRXTests {
def main(args: Array[String]): Unit = {
SimConfig
    .withWave
    .compile(new udpRxSim( udpGenerics = UdpRxGenerics(
      udp_width = 32,//UDP数据位宽
      cnt_width = 6,
      des_port = 37984,
      byte_num =16
    )))
    .doSim{dut=>
      dut.init //接口信号初始化
      dut.test_1
      dut.test_2
      dut.clockDomain.waitSampling(20)
      dut.test_1
    }

// Simulation code here
}
}
