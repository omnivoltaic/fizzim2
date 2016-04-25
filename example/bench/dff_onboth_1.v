// File last modified by Fizzim2 (build 16.03.22) at 2:21:49 PM on 4/26/16

module dff_onboth_1 (
// OUTPUTS
    output reg      r,
    output reg      f,
    output reg      x,
    output reg      g,

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

// dff-ontransit definitions
reg       nx_r;

// Transition combinational always block
always @* begin
    nextstate = state;
    g = 0;
    x = 0;
    nx_r = 0;

    case (state)
        IDLE :
            if(do) begin
                nextstate = RUN;
                g = 1;
            end
        RUN :
            if(!do) begin
                nextstate = LAST;
                x = 1;
            end
        LAST :
            begin
                nextstate = IDLE;
                g = 1;
                nx_r = 1;
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
    r <= nx_r;
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
