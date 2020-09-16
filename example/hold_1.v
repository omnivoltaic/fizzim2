// File last modified by Fizzim2 (build 16.04.26) at 4:25:59 PM on 3/30/20

module hold_1 (

// OUTPUTS hold-onState
    output reg        f,
// OUTPUTS hold-onTransit
    output reg        g,

// INPUTS

// GLOBAL
    input             clk,
    input             rst_n
);

// SIGNALS dff-onState
reg  [3:0] cnt = 4'd0;

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

// hold-onTransit definitions
reg        nx_g = 1'd0;

// Transition combinational always block
always @* begin
    nextstate = state;
    nx_g = g;

    case (state)
        IDLE :
            begin
                nextstate = RUN;
                nx_g = 1;
            end
        RUN :
            if(cnt < 5) begin
                nextstate = RUN;
            end
            else begin
                nextstate = LAST;
            end
        LAST :
            begin
                nextstate = IDLE;
                nx_g = 0;
            end

        default : nextstate = IDLE;
    endcase
end

// Output sequential always block
always @(posedge clk, negedge rst_n)
if (!rst_n) begin
    cnt <= 4'd0;
    g <= 1'd0;
    f <= 1'd0;
end
else begin
    g <= nx_g;
    cnt <= 4'd0;

    case (state)
        RUN : begin
            cnt <= cnt + 1'b1;
        end
        LAST : begin
            f <= ~f;
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
