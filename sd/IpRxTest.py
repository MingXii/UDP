from math import ceil
import os
os.environ['COCOTB_RESOLVE_X'] = 'ZEROS'
import cocotb
import random
from collections import deque
from cocotb.triggers import Timer
from cocotb.triggers import ClockCycles
from cocotb.clock import Clock
from cocotb.result import TestSuccess, TestFailure
from cocotb.triggers import RisingEdge
from queue import Queue
from cocotb.binary import *
#需要引入参考模型

CASES_NUM = 100  # the number of test cases
DES_IP = '17171717'

class IpRxTester:
    def __init__(self, target) -> None:  #-> None为没有返回值
        self.dut = target
        self.taskQ = []  
        self.aimResult = []
        self.recvQ = []

    async def reset_dut(self):
        dut = self.dut
        dut.reset.value = 0
        await RisingEdge(dut.clk)
        dut.reset.value = 1
        for i in range(10):
            await RisingEdge(dut.clk)
        dut.reset.value = 0

    async def generate_input(self):
        dut = self.dut
        cocotb.log.info("get a transaction in Input Driver")
        edge = RisingEdge(dut.clk)
        case_cnt = 0
        while case_cnt < CASES_NUM:
            BYTE_NUM = random.randint(50, 200)
            dut.io_dataIn_valid <= 1
            dut.io_dataIn_payload_fragment <= 0x45
            self.taskQ.append(0x45)
            dut.io_dataOut_ready <= (random.random() > 0.3)
            await edge
            byte_cnt = 0
            while byte_cnt < BYTE_NUM:
                # transaction = random.randint(16, 18)
                transaction = 17
                dut.io_dataOut_ready <= (random.random() > 0.3)
                dut.io_dataIn_valid <= 1
                dut.io_dataIn_payload_fragment <= transaction
                if(dut.io_dataIn_valid.value & dut.io_dataIn_ready.value) == True:
                    self.taskQ.append(transaction)
                    # print(self.taskQ)
                await edge
                byte_cnt = byte_cnt + 1
            last = random.randint(0, 255)
            dut.io_dataIn_valid <= 1
            dut.io_dataOut_ready <= 1
            dut.io_dataIn_payload_fragment <= last
            dut.io_dataIn_payload_last <= 1
            self.taskQ.append(last)
            ipHead = self.taskQ[0:20]
            # print(ipHead)
            desIp = ipHead[16:20]
            # print(desIp)
            strIP = [str(i) for i in desIp]
            desIpStr = ''.join(strIP)
            # print(desIpStr)
            if(desIpStr == DES_IP):
                self.aimResult.append(self.taskQ[20:])
            self.taskQ.clear()
            # print(self.aimResult)
            await edge
            dut.io_dataIn_valid <= 0
            dut.io_dataIn_payload_last <= 0
            await ClockCycles(dut.clk,10,True)
            case_cnt = case_cnt + 1
            print(self.recvQ)

    async def TaskMon(self):
        dut = self.dut
        edge = RisingEdge(dut.clk)
        last_cnt = 0
        if(last_cnt < CASES_NUM):
            if(dut.io_dataOut_valid.value & dut.io_dataOut_ready.value) == True:
                cocotb.log.info("get a transaction in Output Monitor")
                self.recvQ.append(dut.io_dataOut_payload_fragment)
            if dut.io_dataIn_payload_last.value == True:
                last_cnt = last_cnt + 1
            await edge

@cocotb.test()
async def IpRxTest(dut):
    await cocotb.start(Clock(dut.clk, 10, "ns").start())
    # set default values to all dut input ports
    dut.io_dataIn_valid.value = False
    dut.io_dataIn_payload_fragment.value = 0
    dut.io_dataIn_payload_last.value = 0

    dut.io_dataOut_ready = False

    # start testing
    tester = IpRxTester(dut)
    await tester.reset_dut()
    await Timer(100,'ns')
    await cocotb.fork(tester.generate_input())
    await cocotb.fork(tester.TaskMon())

    # assert tester.aimResult == tester.recvQ