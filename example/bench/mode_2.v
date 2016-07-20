
module mode_2 (

// OUTPUTS dff-onState
    output reg        done,

// INPUTS

// GLOBAL
    input             clk,
    input             rst_n
);

// SIGNALS dff-onState
reg        enter = 1'd0;
reg  [3:0] cnt = 4'd0;
// SIGNALS comb-onTransit
reg        exit = 1'd0;

//==========================
// FSM-1
//==========================

// STATE Definitions
parameter
IDLE = 2'd0,
RUN = 2'd1,
LAST = 2'd2;

reg  [1:0] state_1, nextstate_1;
always @(posedge clk, negedge rst_n)
if (!rst_n)
    state_1 <= IDLE;
else
    state_1 <= nextstate_1;

// Transition combinational always block
always @* begin
    nextstate_1 = state_1;

    case (state_1)
        IDLE :
            begin
                nextstate_1 = RUN;
            end
        RUN :
            if(exit) begin
                nextstate_1 = LAST;
            end
        LAST :
            begin
                nextstate_1 = IDLE;
            end
    endcase
end

// Output sequential always block
always @(posedge clk, negedge rst_n)
if (!rst_n) begin
    enter <= 1'd0;
    done <= 1'd0;
end
else begin
    enter <= 1'd0;
    done <= 1'd0;

    case (nextstate_1)
        RUN : begin
            enter <= 1;
        end
        LAST : begin
            done <= 1;
        end
    endcase
end

// This code allows you to see state names in simulation
`ifndef SYNTHESIS
reg [31:0] state_1_name;
always @* begin
    case (state_1)
        IDLE : state_1_name = "IDLE";
        RUN : state_1_name = "RUN";
        LAST : state_1_name = "LAST";
        default : state_1_name = "XXX";
    endcase
end
`endif

//==========================
// FSM-2
//==========================

// STATE Definitions
parameter
S0 = 1'd0,
S1 = 1'd1;

reg  state_2, nextstate_2;
always @(posedge clk, negedge rst_n)
if (!rst_n)
    state_2 <= S0;
else
    state_2 <= nextstate_2;

// Transition combinational always block
always @* begin
    nextstate_2 = state_2;
    exit = 1'd0;

    case (state_2)
        S0 :
            if(enter) begin
                nextstate_2 = S1;
            end
        S1 :
            if(cnt >= 9) begin
                nextstate_2 = S0;
                exit = 1;
            end
    endcase
end

// Output sequential always block
always @(posedge clk, negedge rst_n)
if (!rst_n) begin
    cnt <= 4'd0;
end
else begin
    cnt <= 4'd0;

    case (nextstate_2)
        S1 : begin
            cnt <= cnt + 1'b1;
        end
    endcase
end

// This code allows you to see state names in simulation
`ifndef SYNTHESIS
reg [31:0] state_2_name;
always @* begin
    case (state_2)
        S0 : state_2_name = "S0";
        S1 : state_2_name = "S1";
        default : state_2_name = "XXX";
    endcase
end
`endif

endmodule // Fizzim2
