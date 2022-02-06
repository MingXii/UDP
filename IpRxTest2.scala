package udp_master

import spinal.core._
import spinal.core.sim._
import org.scalatest.funsuite.AnyFunSuite
import scala.util.Random

object IpRxTestConstant {
  val IP_VERSION_WIDTH = 4
  val IHL_WIDTH = 4
  val TOS_WIDTH = 8
  val TOTAL_LENGTH_WIDTH = 16
  val IDENTIFICATION_WIDTH = 16
  val FLAG_WIDTH = 3
  val FRAGMENT_OFFSET_WIDTH = 13
  val TTL_WIDTH = 8
  val PROTOCOL_WIDTH = 8
  val HEADER_CHECKSUM_WIDTH = 16
  val SRC_IP_WIDTH = 32
  val DES_IP_WIDTH = 32
  val IP_HEAD_WIDTH = 32
}

class IpRxTest extends AnyFunSuite {
  val simCfg = SimConfig.allOptimisation.withWave.withVerilator
    .compile(
      new IpRx(
        ipGenerics = IpRxGenerics(
          desIp = 0x12345678
        )
      )
    )

  val ipVersion = Bits(IpRxTestConstant.IP_VERSION_WIDTH bits)
  val ihl = Bits(IpRxTestConstant.IHL_WIDTH bits)
  val tos = Bits(IpRxTestConstant.TOS_WIDTH bits)
  val totalLength = Bits(IpRxTestConstant.TOTAL_LENGTH_WIDTH bits)
  val identification = UInt(IpRxTestConstant.IDENTIFICATION_WIDTH bits)
  val flag = Bits(IpRxTestConstant.FLAG_WIDTH bits)
  val fragmentOffset = Bits(IpRxTestConstant.FRAGMENT_OFFSET_WIDTH bits)
  val ttl = Bits(IpRxTestConstant.TTL_WIDTH bits)
  val protocol = Bits(IpRxTestConstant.PROTOCOL_WIDTH bits)
  val headerChecksum = Bits(IpRxTestConstant.HEADER_CHECKSUM_WIDTH bits)
  val srcIp = Bits(IpRxTestConstant.SRC_IP_WIDTH bits)
  val desIp = Bits(IpRxTestConstant.DES_IP_WIDTH bits)

  val ipHead = Vec(Bits(IpRxTestConstant.IP_HEAD_WIDTH bits), 5)

  test("testbench") {
    simCfg.doSim { dut =>
      def init() = {
        dut.clockDomain.forkStimulus(10)
        dut.io.dataIn.valid #= false
        dut.io.dataOut.ready #= false
        dut.clockDomain.waitSampling(10)
      }
      def ipHeadTest() = {
        ipVersion := 0x4
        ihl := 0x5
        tos := 0x00
        totalLength := Random.nextInt(256)
        identification := 0x0000
        flag := 2
        fragmentOffset := 0
        ttl := 0x40
        protocol := 0x11
        headerChecksum := Random.nextInt(256)
        srcIp := Random.nextLong()
        desIp := Random.nextLong()
        ipHead(0) := ipVersion ## ihl ## tos ## totalLength
        ipHead(1) := identification ## flag ## fragmentOffset
        ipHead(2) := ttl ## protocol ## headerChecksum
        ipHead(3) := srcIp
        ipHead(4) := desIp

        for (i <- 0 to 4) {
          dut.io.dataIn.valid #= true
          for (idx <- 0 to 3) {
            dut.io.dataIn.fragment #= ipHead(i)(
              ((4 - idx) * 8 - 1) downto ((3 - idx) * 8)
            ).toInt
            dut.clockDomain.waitSampling()
          }
        }
      }
    
      def dataTest() = {
        dut.io.dataIn.valid #= true
        for (j <- 0 to 100) {
          dut.io.dataIn.fragment #= Random.nextInt(256)
          dut.io.dataOut.ready.randomize()
          dut.clockDomain.waitSampling()
        }
      }
      init() //接口信号初始化
      //SimTimeout(10000)
      ipHeadTest()
      dataTest()
    /*      dut.clockDomain.waitSampling(20)
      test_1()
      dut.clockDomain.waitSampling(1000)*/
    }
  }
}
