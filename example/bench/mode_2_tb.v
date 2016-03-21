// File last modified by Fizzim2 (build 16.03.22) at 19:55:33 AM on 3/24/16

module mode_2_tb;


//========================================
//
// Instantiation: mode_2
//
//========================================
wire        done;
reg         clk = 1'b0;
reg         rst_n = 1'b0;

mode_2  inst (
    .done  ( done  ), // O
    .clk   ( clk   ), // I
    .rst_n ( rst_n )  // I
); // instantiation of mode_2


always #5 clk = !clk;

initial begin
    $dumpvars(0, mode_2_tb);
    #20 rst_n = 1'b1;
    #150 $finish();
end

endmodule // Fizzim2
