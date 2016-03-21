// File last modified by Fizzim2 (build 16.03.22) at 11:18:56 AM on 3/24/16

module hold_2 (
// OUTPUTS
    output reg      g,
    output reg      f,

// INPUTS

// GLOBAL
    input     clk,
    input     rst_n
);

// SIGNALS
reg [3:0] cnt = 0;

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

// ontransit-hold definitions
reg       nx_g;

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
            end
    endcase
end

// Output sequential always block
always @(posedge clk, negedge rst_n)
if (!rst_n) begin
    g <= 0;
    cnt <= 0;
    f <= 0;
end
else begin
    g <= nx_g;
    cnt <= 0;

    case (nextstate)
        RUN : begin
            cnt <= cnt + 1'b1;
        end
        LAST : begin
            g <= 0;
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
