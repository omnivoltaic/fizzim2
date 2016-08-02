
module spi_master_control (

// OUTPUTS dff-onState
    output reg        spi_end,
    output reg        SPI_CLK,
// OUTPUTS hold-onTransit
    output reg [31:0] spi_idata,
// OUTPUTS buffer
    output reg        SPI_MO,

// INPUTS
    input             spi_start,
    input       [3:0] spi_len,
    input       [3:0] spi_period,
    input             SPI_MI,
    input             spi_loop,
    input      [31:0] spi_odata,

// GLOBAL
    input             clk
);

// SIGNALS dff-onTransit
reg  [3:0] count_period = 4'd0;
// SIGNALS hold-onTransit
reg  [5:0] count_bit = 6'd0;
reg        spi_mo_t = 1'd0;
// SIGNALS buffer
reg        spi_mi_t = 1'd0;

// STATE Definitions
parameter
IDLE = 3'd0,
START = 3'd1,
NEG = 3'd2,
POS = 3'd3,
WAIT = 3'd4;

reg  [2:0] state, nextstate;
always @(posedge clk)
if (!spi_start)
    state <= IDLE;
else
    state <= nextstate;

// dff-onTransit definitions
reg  [3:0] nx_count_period = 4'd0;

// hold-onTransit definitions
reg  [5:0] nx_count_bit = 6'd0;
reg [31:0] nx_spi_idata = 32'd0;
reg        nx_spi_mo_t = 1'd0;

// Transition combinational always block
always @* begin
    nextstate = state;
    nx_count_period = 4'd0;
    nx_count_bit = count_bit;
    nx_spi_idata = spi_idata;
    nx_spi_mo_t = spi_mo_t;

    case (state)
        IDLE :
            if(spi_start) begin
                nextstate = START;
            end
        START :
            if(spi_start) begin
                nextstate = POS;
                nx_count_period = spi_period;
                nx_count_bit = 0;
            end
            else begin
                nextstate = IDLE;
            end
        NEG :
            if(count_period == spi_period) begin
                nextstate = POS;
                nx_count_bit = count_bit + 1'b1;
                nx_spi_idata = {spi_idata[30:0], spi_loop ? spi_mo_t : spi_mi_t};
            end
            else begin
                nextstate = NEG;
                nx_count_period = count_period + 1'b1;
            end
        POS :
            if(count_period != spi_period) begin
                nextstate = POS;
                nx_count_period = count_period + 1'b1;
            end
            else if((spi_len == 4'hf) ? (count_bit >= 32) : (count_bit > spi_len)) begin
                nextstate = WAIT;
            end
            else begin
                nextstate = NEG;
                nx_spi_mo_t = spi_odata[6'd31 - count_bit];
            end
        WAIT :
            if(!spi_start) begin
                nextstate = IDLE;
            end
    endcase
end

// Output sequential always block
always @(posedge clk)
begin
    SPI_MO <= spi_mo_t;
    spi_mi_t <= SPI_MI;
    count_period <= nx_count_period;
    count_bit <= nx_count_bit;
    spi_idata <= nx_spi_idata;
    spi_mo_t <= nx_spi_mo_t;
    SPI_CLK <= 1'b1;
    spi_end <= 1'd0;

    case (nextstate)
        NEG : begin
            SPI_CLK <= 1'b0;
        end
        WAIT : begin
            spi_end <= 1'b1;
        end
    endcase
end

// This code allows you to see state names in simulation
`ifndef SYNTHESIS
reg [31:0] state_name;
always @* begin
    case (state)
        IDLE : state_name = "IDLE";
        START : state_name = "START";
        NEG : state_name = "NEG";
        POS : state_name = "POS";
        WAIT : state_name = "WAIT";
        default : state_name = "XXX";
    endcase
end
`endif

endmodule // Fizzim2
