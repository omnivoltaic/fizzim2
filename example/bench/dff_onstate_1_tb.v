// File last modified by Fizzim2 (build 16.03.22) at 19:55:33 AM on 3/24/16

module dff_onstate_1_tb;

//========================================
//
// Instantiation: dff_onstate_1
//
//========================================
wire        f;
reg         do = 1'b0;
reg         clk = 1'b0;
reg         rst_n = 1'b0;

dff_onstate_1  inst (
    .f     ( f     ), // O
    .do    ( do    ), // I
    .clk   ( clk   ), // I
    .rst_n ( rst_n )  // I
); // instantiation of dff_onstate_1

always #5 clk = !clk;

initial begin
    $dumpvars(0, dff_onstate_1_tb);
    #20 rst_n = 1'b1;
    #20  do = 1'b1;
    #100 do = 1'b0;
    #40 $finish();
end

endmodule // Fizzim2
