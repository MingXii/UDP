package workshop.udp_master

import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import workshop.common.WorkshopSimConfig
class UdpRxTester extends FunSuite {
  var compiled: SimCompiled[UdpRx] = null

  test("compile") {
    compiled = WorkshopSimConfig()
      .compile(
        UdpRx(
          udpGenerics = UdpRxGenerics(
            udpWidth = 32, //UDP数据位宽
            cntWidth = 6,
            desPort = 37984,
            byteNum = 16
          )
        )
      )
  }

  test("testbench") {
    compiled.doSim { dut =>
      def init = {
        dut.clockDomain.forkStimulus(10)
        dut.io.macRxdata.valid #= false
        dut.io.rx.ready #= false
        dut.clockDomain.waitSampling(10)
      }
      def test_1 = {
        dut.io.macRxdata.valid #= true
        dut.io.macRxdata.fragment #= 0x12
        dut.clockDomain.waitSampling()
        dut.io.macRxdata.fragment #= 0x34
        dut.clockDomain.waitSampling()
        dut.io.macRxdata.fragment #= 0x94
        dut.clockDomain.waitSampling()
        dut.io.macRxdata.fragment #= 0x60
        dut.clockDomain.waitSampling()
        dut.io.macRxdata.fragment #= 0x00
        dut.clockDomain.waitSampling()
        dut.io.macRxdata.fragment #= 0x64
        dut.clockDomain.waitSampling()
        dut.io.macRxdata.fragment #= 0x00
        dut.clockDomain.waitSampling()
        dut.io.macRxdata.fragment #= 0x00
      }
      def test_2 = {
        dut.io.macRxdata.valid #= true
        dut.io.rx.ready #= true
        dut.io.macRxdata.fragment #= 0x12
        dut.clockDomain.waitSampling(20)
        dut.io.macRxdata.fragment #= 0x34
        dut.io.rx.ready #= false
        dut.clockDomain.waitSampling(20)
        dut.io.rx.ready #= true
        dut.clockDomain.waitSampling(20)
        dut.io.macRxdata.fragment #= 0x56
        dut.clockDomain.waitSampling(90)
        dut.io.macRxdata.valid #= false
      }
      //dut.io.byteNum#=32
      init //接口信号初始化
      //SimTimeout(10000)
      test_1
      test_2
      dut.clockDomain.waitSampling(20)
      test_1
      dut.clockDomain.waitSampling(1000)
    }
  }
}
