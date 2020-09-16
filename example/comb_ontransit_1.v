// File last modified by Fizzim2 (build 16.04.26) at 6:28:08 PM on 8/30/20

module comb_ontransit_1 (

// OUTPUTS comb-onTransit
    output reg        g,
    output reg        s,

// INPUTS
    input             do,

// GLOBAL
    input             clk,
    input             rst_n
);

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
    g = 1'd0;
    s = 1'd0;

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
            else begin
                nextstate = RUN;
                s = 1;
            end
        LAST :
            begin
                nextstate = IDLE;
            end

        default : nextstate = IDLE;
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
