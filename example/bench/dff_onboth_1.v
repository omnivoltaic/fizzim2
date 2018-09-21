
module dff_onboth_1 (

// OUTPUTS dff-onState
    output reg        f,
// OUTPUTS comb-onTransit
    output reg        x,
    output reg        g,
// OUTPUTS dff-onBoth
    output reg        r,

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

// dff-onTransit definitions
reg        nx_r = 1'd0;

// Transition combinational always block
always @* begin
    nextstate = state;
    nx_r = 1'd0;
    g = 1'd0;
    x = 1'd0;

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

        default : nextstate = IDLE;
    endcase
end

// Output sequential always block
always @(posedge clk, negedge rst_n)
if (!rst_n) begin
    f <= 1'd0;
    r <= 1'd0;
end
else begin
    r <= nx_r;
    f <= 1'd0;

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
