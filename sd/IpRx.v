// Generator : SpinalHDL v1.6.1    git head : 3bf789d53b1b5a36974196e2d591342e15ddf28c
// Component : IpRx

`timescale 1ns/1ps 

module IpRx (
  input               io_dataIn_valid,
  output reg          io_dataIn_ready,
  input               io_dataIn_payload_last,
  input      [7:0]    io_dataIn_payload_fragment,
  output reg          io_dataOut_valid,
  input               io_dataOut_ready,
  output reg          io_dataOut_payload_last,
  output reg [7:0]    io_dataOut_payload_fragment,
  input               clk,
  input               reset
);
  localparam fsm_enumDef_BOOT = 2'd0;
  localparam fsm_enumDef_idle = 2'd1;
  localparam fsm_enumDef_ip2udp = 2'd2;
  localparam fsm_enumDef_rx_end = 2'd3;

  wire       [5:0]    _zz_when_IpRx_l54;
  wire       [5:0]    _zz_when_IpRx_l54_1;
  reg        [4:0]    cnt;
  reg        [5:0]    ipHeadByteNum;
  reg        [31:0]   desIp;
  wire                fsm_wantExit;
  reg                 fsm_wantStart;
  wire                fsm_wantKill;
  reg        [1:0]    fsm_stateReg;
  reg        [1:0]    fsm_stateNext;
  wire                when_IpRx_l43;
  wire                when_IpRx_l53;
  wire                when_IpRx_l54;
  wire                when_IpRx_l45;
  wire                when_IpRx_l47;
  wire                when_IpRx_l79;
  wire                when_StateMachine_l235;
  `ifndef SYNTHESIS
  reg [47:0] fsm_stateReg_string;
  reg [47:0] fsm_stateNext_string;
  `endif


  assign _zz_when_IpRx_l54 = {1'd0, cnt};
  assign _zz_when_IpRx_l54_1 = (ipHeadByteNum - 6'h01);
  `ifndef SYNTHESIS
  always @(*) begin
    case(fsm_stateReg)
      fsm_enumDef_BOOT : fsm_stateReg_string = "BOOT  ";
      fsm_enumDef_idle : fsm_stateReg_string = "idle  ";
      fsm_enumDef_ip2udp : fsm_stateReg_string = "ip2udp";
      fsm_enumDef_rx_end : fsm_stateReg_string = "rx_end";
      default : fsm_stateReg_string = "??????";
    endcase
  end
  always @(*) begin
    case(fsm_stateNext)
      fsm_enumDef_BOOT : fsm_stateNext_string = "BOOT  ";
      fsm_enumDef_idle : fsm_stateNext_string = "idle  ";
      fsm_enumDef_ip2udp : fsm_stateNext_string = "ip2udp";
      fsm_enumDef_rx_end : fsm_stateNext_string = "rx_end";
      default : fsm_stateNext_string = "??????";
    endcase
  end
  `endif

  always @(*) begin
    io_dataIn_ready = 1'b0;
    case(fsm_stateReg)
      fsm_enumDef_idle : begin
        if(io_dataIn_valid) begin
          io_dataIn_ready = 1'b1;
        end
      end
      fsm_enumDef_ip2udp : begin
        if(io_dataIn_valid) begin
          io_dataIn_ready = io_dataOut_ready;
        end
      end
      fsm_enumDef_rx_end : begin
      end
      default : begin
      end
    endcase
  end

  always @(*) begin
    io_dataOut_valid = 1'b0;
    case(fsm_stateReg)
      fsm_enumDef_idle : begin
      end
      fsm_enumDef_ip2udp : begin
        if(io_dataIn_valid) begin
          io_dataOut_valid = io_dataIn_valid;
        end
      end
      fsm_enumDef_rx_end : begin
      end
      default : begin
      end
    endcase
  end

  always @(*) begin
    io_dataOut_payload_fragment = 8'h0;
    case(fsm_stateReg)
      fsm_enumDef_idle : begin
      end
      fsm_enumDef_ip2udp : begin
        if(io_dataIn_valid) begin
          io_dataOut_payload_fragment = io_dataIn_payload_fragment;
        end
      end
      fsm_enumDef_rx_end : begin
      end
      default : begin
      end
    endcase
  end

  always @(*) begin
    io_dataOut_payload_last = 1'b0;
    case(fsm_stateReg)
      fsm_enumDef_idle : begin
      end
      fsm_enumDef_ip2udp : begin
        if(io_dataIn_valid) begin
          io_dataOut_payload_last = io_dataIn_payload_last;
        end
      end
      fsm_enumDef_rx_end : begin
      end
      default : begin
      end
    endcase
  end

  assign fsm_wantExit = 1'b0;
  always @(*) begin
    fsm_wantStart = 1'b0;
    case(fsm_stateReg)
      fsm_enumDef_idle : begin
      end
      fsm_enumDef_ip2udp : begin
      end
      fsm_enumDef_rx_end : begin
      end
      default : begin
        fsm_wantStart = 1'b1;
      end
    endcase
  end

  assign fsm_wantKill = 1'b0;
  always @(*) begin
    fsm_stateNext = fsm_stateReg;
    case(fsm_stateReg)
      fsm_enumDef_idle : begin
        if(io_dataIn_valid) begin
          if(!when_IpRx_l43) begin
            if(!when_IpRx_l45) begin
              if(when_IpRx_l47) begin
                if(when_IpRx_l53) begin
                  if(when_IpRx_l54) begin
                    fsm_stateNext = fsm_enumDef_ip2udp;
                  end
                end else begin
                  fsm_stateNext = fsm_enumDef_rx_end;
                end
              end
            end
          end
        end
      end
      fsm_enumDef_ip2udp : begin
        if(!io_dataIn_valid) begin
          fsm_stateNext = fsm_enumDef_rx_end;
        end
      end
      fsm_enumDef_rx_end : begin
        if(when_IpRx_l79) begin
          fsm_stateNext = fsm_enumDef_idle;
        end
      end
      default : begin
      end
    endcase
    if(fsm_wantStart) begin
      fsm_stateNext = fsm_enumDef_idle;
    end
    if(fsm_wantKill) begin
      fsm_stateNext = fsm_enumDef_BOOT;
    end
  end

  assign when_IpRx_l43 = (cnt == 5'h0);
  assign when_IpRx_l53 = ({desIp[23 : 0],io_dataIn_payload_fragment} == 32'h11111111);
  assign when_IpRx_l54 = (_zz_when_IpRx_l54 == _zz_when_IpRx_l54_1);
  assign when_IpRx_l45 = ((5'h10 <= cnt) && (cnt <= 5'h12));
  assign when_IpRx_l47 = (cnt == 5'h13);
  assign when_IpRx_l79 = (! io_dataIn_valid);
  assign when_StateMachine_l235 = ((! (fsm_stateReg == fsm_enumDef_idle)) && (fsm_stateNext == fsm_enumDef_idle));
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      cnt <= 5'h0;
      fsm_stateReg <= fsm_enumDef_BOOT;
    end else begin
      fsm_stateReg <= fsm_stateNext;
      case(fsm_stateReg)
        fsm_enumDef_idle : begin
          if(io_dataIn_valid) begin
            cnt <= (cnt + 5'h01);
            if(!when_IpRx_l43) begin
              if(!when_IpRx_l45) begin
                if(when_IpRx_l47) begin
                  if(when_IpRx_l53) begin
                    if(when_IpRx_l54) begin
                      cnt <= 5'h0;
                    end
                  end else begin
                    cnt <= 5'h0;
                  end
                end
              end
            end
          end
        end
        fsm_enumDef_ip2udp : begin
        end
        fsm_enumDef_rx_end : begin
        end
        default : begin
        end
      endcase
    end
  end

  always @(posedge clk) begin
    case(fsm_stateReg)
      fsm_enumDef_idle : begin
        if(io_dataIn_valid) begin
          if(when_IpRx_l43) begin
            ipHeadByteNum <= ({2'd0,io_dataIn_payload_fragment[3 : 0]} <<< 2);
          end else begin
            if(when_IpRx_l45) begin
              desIp <= {desIp[23 : 0],io_dataIn_payload_fragment};
            end else begin
              if(when_IpRx_l47) begin
                desIp <= {desIp[23 : 0],io_dataIn_payload_fragment};
              end
            end
          end
        end
      end
      fsm_enumDef_ip2udp : begin
      end
      fsm_enumDef_rx_end : begin
      end
      default : begin
      end
    endcase
    if(when_StateMachine_l235) begin
      ipHeadByteNum <= 6'h0;
      desIp <= 32'h0;
    end
  end
  // Dump waves
  initial begin
    $dumpfile ("IpRx.vcd");
    $dumpvars (1,IpRx);
  end


endmodule
