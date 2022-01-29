package workshop.udp_master

import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import workshop.common.WorkshopSimConfig
class UdpTxTester extends FunSuite {
  var compiled: SimCompiled[UdpTx] = null

  test("compile") {
    compiled = WorkshopSimConfig()
      .addRtl("rtl/Crc32_d8.v")
      .compile(
        UdpTx(
          udpGenerics = UdpTxGenerics(
            udpWidth = 32, //UDP数据位宽
            cntWidth = 6,
            srcPort = 37984,
            byteNum = 16
          )
        )
      )
  }

  test("testbench") {
    compiled.doSim { dut =>
      def init = {
        dut.clockDomain.forkStimulus(10)
        dut.io.macTxdata.valid #= false
        dut.io.gthTxdata.ready #= false
        dut.clockDomain.waitSampling(10)
      }
      def test_1 = {
        dut.io.macTxdata.valid #= true
        dut.io.macTxdata.fragment #= 0x1234
        //dut.io.byteNum#=32
        dut.io.dstPort #= 0x9460
        dut.clockDomain.waitSampling()
        dut.io.macTxdata.fragment #= 0x3478
        dut.clockDomain.waitSampling()
        dut.io.macTxdata.fragment #= 0x9423
        dut.clockDomain.waitSampling()
        dut.io.macTxdata.fragment #= 0x6056
        dut.clockDomain.waitSampling()
        dut.io.macTxdata.fragment #= 0x0075
        dut.clockDomain.waitSampling()
        dut.io.macTxdata.fragment #= 0x6423
        dut.clockDomain.waitSampling()
        dut.io.macTxdata.fragment #= 0x0045
        dut.clockDomain.waitSampling()
        dut.io.macTxdata.fragment #= 0x0089
      }
      def test_2 = {
        dut.io.macTxdata.valid #= true
        dut.io.gthTxdata.ready #= true
        dut.io.macTxdata.fragment #= 0x1213
        dut.clockDomain.waitSampling(20)
        dut.io.macTxdata.fragment #= 0x3456
        dut.io.gthTxdata.ready #= false
        dut.clockDomain.waitSampling(20)
        dut.io.gthTxdata.ready #= true
        dut.clockDomain.waitSampling(20)
        dut.io.macTxdata.fragment #= 0x5678
        dut.clockDomain.waitSampling(90)
        dut.io.macTxdata.valid #= false
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
