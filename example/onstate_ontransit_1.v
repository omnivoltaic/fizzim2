// File last modified by Fizzim2 (build 16.03.22) at 10:02:25 AM on 3/24/16

module onstate_ontransit_1 (
// OUTPUTS
    output reg      g,
    output reg      f,
    output reg      r,

// INPUTS
    input           do,

// GLOBAL
    input     clk,
    input     rst_n
);

// SIGNALS

// STATE Definitions
parameter
IDLE = 2'd0,
RUN = 2'd1,
LAST = 2'd2;

reg  [1:0] state, nextstate;
always @(posedge clk, negedge rst_n)
if (!rst_n)
    state <= IDLE;
else
    state <= nextstate;

// Transition combinational always block
always @* begin
    nextstate = state;
    g = 0;

    case (state)
        IDLE :
            if(do) begin
                nextstate = RUN;
            end
        RUN :
            if(!do) begin
                nextstate = LAST;
                g = 1;
            end
        LAST :
            begin
                nextstate = IDLE;
            end
    endcase
end

// Output sequential always block
always @(posedge clk, negedge rst_n)
if (!rst_n) begin
    r <= 0;
    f <= 0;
end
else begin
    r <= 0;
    f <= 0;

    case (nextstate)
        RUN : begin
            r <= 1;
        end
        LAST : begin
            f <= 1;
        end
    endcase
end

// This code allows you to see state names in simulation
`ifndef SYNTHESIS
reg [31:0] state_name;
always @* begin
    case (state)
        IDLE : state_name = "IDLE";
        RUN : state_name = "RUN";
        LAST : state_name = "LAST";
        default : state_name = "XXX";
    endcase
end
`endif

endmodule // Fizzim2
