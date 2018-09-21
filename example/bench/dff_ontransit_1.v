
module dff_ontransit_1 (

// OUTPUTS dff-onTransit
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

// dff-onTransit definitions
reg        nx_g = 1'd0;
reg        nx_s = 1'd0;

// Transition combinational always block
always @* begin
    nextstate = state;
    nx_g = 1'd0;
    nx_s = 1'd0;

    case (state)
        IDLE :
            if(do) begin
                nextstate = RUN;
            end
        RUN :
            if(!do) begin
                nextstate = LAST;
                nx_g = 1;
            end
            else begin
                nextstate = RUN;
                nx_s = 1;
            end
        LAST :
            begin
                nextstate = IDLE;
            end

        default : nextstate = IDLE;
    endcase
end

// Output sequential always block
always @(posedge clk, negedge rst_n)
if (!rst_n) begin
    g <= 1'd0;
    s <= 1'd0;
end
else begin
    g <= nx_g;
    s <= nx_s;
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
