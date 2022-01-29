package workshop.udp_master

import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import workshop.common.WorkshopSimConfig
class IpRxTest extends FunSuite {
  var compiled: SimCompiled[IpRx] = null

  test("compile") {
    compiled = WorkshopSimConfig().compile(
      IpRx(
        ipGenerics = IpRxGenerics(
          ipWidth = 8, //UDP数据位宽
          cntWidth = 5,
          desIp = 0x12345678,
          byteNum = 16
        )
      )
    )
  }

  test("testbench") {
    compiled.doSim { dut =>
      def init = {
        dut.clockDomain.forkStimulus(10)
        dut.io.ipRxDataIn.valid #= false
        dut.io.ipRxDataOut.ready #= false
        dut.clockDomain.waitSampling(10)
      }
      def test_1 = {
        dut.io.ipRxDataIn.valid #= true
        dut.io.ipRxDataIn.fragment #= 0x45 //
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x34
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x94
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x56
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x75
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x64
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x45
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x89
        dut.clockDomain.waitSampling(9)
        dut.io.ipRxDataIn.fragment #= 0x12
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x34
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x56
        dut.clockDomain.waitSampling()
        dut.io.ipRxDataIn.fragment #= 0x78
        dut.clockDomain.waitSampling()
      }
      def test_2 = {
        dut.io.ipRxDataIn.valid #= true
        dut.io.ipRxDataOut.ready #= true
        dut.io.ipRxDataIn.fragment #= 0x13
        dut.clockDomain.waitSampling(20)
        dut.io.ipRxDataIn.fragment #= 0x34
        dut.io.ipRxDataOut.ready #= false
        dut.clockDomain.waitSampling(20)
        dut.io.ipRxDataOut.ready #= true
        dut.clockDomain.waitSampling(20)
        dut.io.ipRxDataIn.fragment #= 0x56
        dut.clockDomain.waitSampling(90)
        dut.io.ipRxDataIn.valid #= false
      }
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
