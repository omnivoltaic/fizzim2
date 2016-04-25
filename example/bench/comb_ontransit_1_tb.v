// File last modified by Fizzim2 (build 16.03.22) at 19:55:33 AM on 3/24/16

module comb_ontransit_1_tb;

//========================================
//
// Instantiation: comb_ontransit_1
//
//========================================
wire        g, s;
reg         do = 1'b0;
reg         clk = 1'b0;
reg         rst_n = 1'b0;

comb_ontransit_1  inst (
    .g     ( g     ), // O
    .s     ( s     ), // O
    .do    ( do    ), // I
    .clk   ( clk   ), // I
    .rst_n ( rst_n )  // I
); // instantiation of comb_ontransit_1

always #5 clk = !clk;

initial begin
    $dumpvars(0, comb_ontransit_1_tb);
    #20 rst_n = 1'b1;
    #16  do = 1'b1;
    #100 do = 1'b0;
    #40 $finish();
end

endmodule // Fizzim2
