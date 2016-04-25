// File last modified by Fizzim2 (build 16.03.22) at 19:55:33 AM on 3/24/16

module dff_onboth_1_tb;

//========================================
//
// Instantiation: dff_onboth_1
//
//========================================
wire        f, r, x, g;
reg         do = 1'b0;
reg         clk = 1'b0;
reg         rst_n = 1'b0;

dff_onboth_1  inst (
    .f     ( f     ), // O
    .r     ( r     ), // O
    .x     ( x     ), // O
    .g     ( g     ), // O
    .do    ( do    ), // I
    .clk   ( clk   ), // I
    .rst_n ( rst_n )  // I
); // instantiation of dff_onboth_1


always #5 clk = !clk;

initial begin
    $dumpvars(0, dff_onboth_1_tb);
    #20 rst_n = 1'b1;
    #16  do = 1'b1;
    #100 do = 1'b0;
    #50 $finish();
end

endmodule // Fizzim2
