
module priority_1 (

// OUTPUTS dff-onState
    output reg        f,

// INPUTS
    input             do,
    input       [1:0] sel,

// GLOBAL
    input             clk,
    input             rst_n
);

// STATE Definitions
parameter
IDLE = 2'd0,
RUN = 2'd1,
LAST = 2'd2,
MIDDLE = 2'd3;

reg  [1:0] state, nextstate;
always @(posedge clk, negedge rst_n)
if (!rst_n)
    state <= IDLE;
else
    state <= nextstate;

// Transition combinational always block
always @* begin
    nextstate = state;

    case (state)
        IDLE :
            if(do) begin
                nextstate = RUN;
            end
        RUN :
            if(!do) begin
                nextstate = MIDDLE;
            end
        LAST :
            begin
                nextstate = IDLE;
            end
        MIDDLE :
            if(do) begin
                nextstate = RUN;
            end
            else if(sel==2'd2) begin
                nextstate = IDLE;
            end
            else if(sel==2'd3) begin
                nextstate = LAST;
            end
    endcase
end

// Output sequential always block
always @(posedge clk, negedge rst_n)
if (!rst_n) begin
    f <= 1'd0;
end
else begin
    f <= 1'd0;

    case (nextstate)
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
        MIDDLE : state_name = "MIDDLE";
        default : state_name = "XXX";
    endcase
end
`endif

endmodule // Fizzim2
