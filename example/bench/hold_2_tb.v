// File last modified by Fizzim2 (build 16.03.22) at 19:55:33 AM on 3/24/16

module hold_2_tb;


//========================================
//
// Instantiation: hold_2
//
//========================================
wire        g;
wire        f;
reg         clk = 1'b0;
reg         rst_n = 1'b0;

hold_2  inst (
    .g     ( g     ), // O
    .f     ( f     ), // O
    .clk   ( clk   ), // I
    .rst_n ( rst_n )  // I
); // instantiation of hold_2


always #5 clk = !clk;

initial begin
    $dumpvars(0, hold_2_tb);
    #20 rst_n = 1'b1;
    #150 $finish();
end

endmodule // Fizzim2
