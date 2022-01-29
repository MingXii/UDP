package workshop.udp_master

import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import workshop.common.WorkshopSimConfig
class MacRxTest extends FunSuite {
  var compiled: SimCompiled[MacRx] = null

  test("compile") {
    compiled = WorkshopSimConfig().compile(
      MacRx(
        macGenerics = MacRxGenerics(
          macWidth = 8, //UDP数据位宽
          cntWidth = 6,
          preamble = 0x55, //8bit
          sfd = 0xd5,
          desMac = 0x123456789012L,
          ethType = 0x0800,
          byteNum = 16
        )
      )
    )
  }

  test("testbench") {
    compiled.doSim { dut =>
      def init = {
        dut.clockDomain.forkStimulus(10)
        dut.io.macRxDataIn.valid #= false
        dut.io.macRxDataOut.ready #= false
        dut.clockDomain.waitSampling(10)
      }
      def test_1 = {
        dut.io.macRxDataIn.valid #= true
        dut.io.macRxDataIn.fragment #= 0x45 //
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x55
        dut.clockDomain.waitSampling(7)
        dut.io.macRxDataIn.fragment #= 0xd5
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x12
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x34
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x56
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x78
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x90
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x12
        dut.clockDomain.waitSampling(7)
        dut.io.macRxDataIn.fragment #= 0x08
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x00
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x56
        dut.clockDomain.waitSampling()
        dut.io.macRxDataIn.fragment #= 0x78
        dut.clockDomain.waitSampling()
      }
      def test_2 = {
        dut.io.macRxDataIn.valid #= true
        dut.io.macRxDataOut.ready #= true
        dut.io.macRxDataIn.fragment #= 0x13
        dut.clockDomain.waitSampling(20)
        dut.io.macRxDataIn.fragment #= 0x34
        dut.io.macRxDataOut.ready #= false
        dut.clockDomain.waitSampling(20)
        dut.io.macRxDataOut.ready #= true
        dut.clockDomain.waitSampling(20)
        dut.io.macRxDataIn.fragment #= 0x56
        dut.clockDomain.waitSampling(90)
        dut.io.macRxDataIn.valid #= false
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
