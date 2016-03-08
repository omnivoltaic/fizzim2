import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.JOptionPane;

import java.text.DateFormat;
import java.util.Date;

/*
Copyright 2016  tobalanx@qq.com

This file is part of Fizzim2.

Fizzim2 is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

Fizzim2 is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

public class GenerateHDL {

    LinkedList<LinkedList<ObjAttribute>> globalList = null;
    Vector<Object> objList;

    File file;
    javax.swing.JTextArea consoleText;
    String modName;
    String path;
    String clkName, clkEdge;
    String resetName = null;
    String resetEdge = null;
    String onStateInit = "";
    String onTransitInit = "";
    int stateNum;

    String stateVar = "state";
    String nextsVar = "nextstate";
    String ind = "    ";
    String ind2 = ind + ind, ind3 = ind2 + ind, ind4 = ind2 + ind2;

    public GenerateHDL(String p, DrawArea draw, javax.swing.JTextArea cons)
    {
        String s;
        file = new File(p);
        s = file.getName();
        modName = s.substring(0, s.length() - 2);

        globalList = draw.globalList;
        objList = draw.objList;

        consoleText = cons;
    }

    public boolean save()
    {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            int stateBw = log2(getStateNum());
            String txt = "";
            String alwaysLine = "always @(";
            String resetLine = "";
            String resetState = "";

            Date currDate = new Date();
            long currTime = currDate.getTime();
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            DateFormat dt = DateFormat.getTimeInstance(DateFormat.MEDIUM);
            txt = ("// File last modified by Fizzim2 at " + dt.format(currTime)
                    + " on " + df.format(currDate) + "\n");
            //writer.write("version" + currVer + "\n");

            txt += "\nmodule "+ modName +" (\n";

            // Port lists
            LinkedList<ObjAttribute> tempList;
            ObjAttribute att;
            String[] sa;
            int i, j;
            int t;
            String s;

            txt += "// OUTPUTS\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabOutput);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                sa = nameinfo((String) att.get(0));
                txt += ("    output reg" + sa[2] + " " + sa[1] + ",\n");

                s = (String) att.get(7);
                s = (s.equals("") ? 0 : s) + ";\n";
                if(att.get(3).equals("onstate"))
                {
                    onStateInit += (ind + sa[1] + " <= " + s);
                }
                else
                    onTransitInit += (ind + sa[1] + " = " + s);
            }

            txt += "\n// INPUTS\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabInput);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                sa = nameinfo((String) att.get(0));
                txt += ("    input     " + sa[2] + " " + sa[1] + ",\n");
            }

            txt += "\n// GLOBAL\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabGlobal);
            att = tempList.get(1);
            sa = nameinfo((String) att.get(1));
            txt += ("    input     " + sa[2] + " " + sa[1] + ",\n");
            alwaysLine += ((String) att.get(3)) + " " + sa[1];

            if (tempList.size() > 1) {
                att = tempList.get(2);
                sa = nameinfo((String) att.get(1));
                txt += ("    input     " + sa[2] + " " + sa[1] + "\n");

                if(tempList.get(3).equals("posedge"))
                {
                    alwaysLine += " or posedge " + sa[1];
                    resetLine = "if (" + sa[1] + ")";
                } else
                {
                    alwaysLine += " or negedge " + sa[1];
                    resetLine = "if (!" + sa[1] + ")";
                }
            }
            alwaysLine += ")";

            if (tempList.size() > 2) {
                att = tempList.get(3);
                resetState = (String) att.get(1);
            }

            txt += ");\n";
            txt += "\n// SIGNALS\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabSignal);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                sa = nameinfo((String) att.get(0));
                txt += ("reg " + sa[2] + " " + sa[1] + ";\n");

                s = (String) att.get(7);
                s = (s.equals("") ? 0 : s) + ";\n";
                if(att.get(3).equals("onstate"))
                {
                    onStateInit += (ind + sa[1] + " <= " + s);
                }
                else
                    onTransitInit += (ind + sa[1] + " = " + s);
            }

            GeneralObj obj;

            txt += "\n// STATE Definitions\n";
            t = 0;
            j = 0;
            for(i = 1; i < objList.size(); i++)
            {
                obj = (GeneralObj) objList.get(i);
                if(obj.getType() == 0)
                {
                    if(t>0)
                    {
                        txt += ",\n";
                    }
                    else
                    {
                        t = 1;
                        txt += "parameter\n";
                    }

                    txt += (obj.getName() + " = " + stateBw + "'d" + j);
                    j += 1;
                }
            }
            txt += ";\n";

            s = stateVar + "," + nextsVar + ";\n";
            if(stateBw > 1)
            txt += "\nreg  [" + (stateBw -1) + ":0] " + s;
            else
            txt += "\nreg  " + s;

            txt += alwaysLine + "\n";
            txt += resetLine +
                    "\n    " + stateVar + " <= " + resetState +
                    ";\nelse\n    " + stateVar + " <= " + nextsVar + ";\n";


            LinkedList<ObjAttribute> attribList;
            txt += "\n// Combinational always block\n";
            txt += "always @* begin\n";
            txt += "    " + nextsVar + " = " + stateVar + ";\n";
            txt += onTransitInit;
            txt += "    case (" + stateVar + ")\n";

            for(i = 1; i < objList.size(); i++)
            {
                obj = (GeneralObj) objList.elementAt(i);
                if(obj.getType() != 0) // State Type Only
                continue;

                attribList = obj.getAttributeList();
                att = attribList.get(0);
                s = (String) att.get(1);
                txt += ind2 + s + " :\n" + doTransit(s);
            }
            txt += "    endcase\nend\n";

            txt += "\n// Sequential always block\n";
            txt += alwaysLine + "\n";
            txt += resetLine + " begin\n";
            txt += onStateInit + "end\nelse begin\n";
            txt += onStateInit;
            txt += ind + "case (" + nextsVar + ")\n";

            for(i = 1; i < objList.size(); i++)
            {
                obj = (GeneralObj) objList.elementAt(i);
                if(obj.getType() != 0) // State Type Only
                continue;

                attribList = obj.getAttributeList();
                s = "";
                for (j = attribList.size() -1; j >= 0 ; j--) {
                    att = attribList.get(j);

                    if(j==0)
                    {
                        if(!s.equals(""))
                        s = (ind2 + att.get(1) + " : begin\n") + s + "        end\n";
                    }
                    else if(!att.get(1).equals(""))
                    {
                        sa = nameinfo((String) att.get(0));
                        s += (ind3 + sa[1] + " <= " + att.get(1) + ";\n");
                    }
                }
                txt += s;
            }
            txt += "    endcase\nend\n";

            txt += "\nendmodule // Fizzim2\n";
            writer.write(txt);
            writer.close();
            consoleText.setText(txt);

            return true;

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,//this,
                    "Error generating HDL file",
                    "error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

    }


    public int log2(int d) {
        int r = 1;

        d--;
        while(d >= 2) {
            d = d>>>1;
            r += 1;
        }
        return r;
    }

    public String[] nameinfo(String s)
    {
        s = s.replace(" ", "");
        String log="Error : name '" + s + "' in PORT or SIGNAL definition.\n";
        log +=  "\tIt should be the format such as 'abc' or 'abc[7:0].'";
        int i1, i2, i3;
        i1 = s.indexOf("[");
        i2 = s.indexOf(":");
        i3 = s.indexOf("]");
/*
try {
    Integer.parseInt(str);
} catch(NumberFormatException e)
{
    e.printStackTrace();
}
*/
        String msb=null, lsb=null, bus="     ", width="1";
        String order="0";  // 0:1-bit, 1:MSB>LSB, 2:MSB<LSB
        if(i1>0)
        {
            if(i2>i1 && i3>i2)
            {
                log=null;
                msb = s.substring(i1+1, i2);
                lsb = s.substring(i2+1, i3);
                bus = s.substring(i1, i3+1);
                s = s.substring(0, i1);

                int w = 1;
                w = Integer.parseInt(msb) - Integer.parseInt(lsb);
                if(w<0) {
                    w *= -1;
                    order = "2";
                } else if(w>0)
                {
                    order = "1";
                }

                width = String.valueOf(w+1);
            }
        } else
        {
            if(i1<0 && i2<0 && i3<0)
                log=null;
        }
        //System.out.println("-"+s+"-"+msb+"-"+lsb+"-"+width);
        //System.out.println(log);

        return new String[]{log, s, bus, width, msb, lsb, order};
    }

    public int getStateNum() {
        int r = 0;

        for(int i = 1; i < objList.size(); i++)
        {
            GeneralObj obj = (GeneralObj) objList.elementAt(i);

            if(obj.getType() == 0) // State Type
            r++;
        }

        return r;
    }


    public String doTransit(String stateName)
    {
        String txt = new String();
        String startState = new String();
        String endState = new String();
        String eqn = "1", pri = "0";
        LinkedList<String> list = new LinkedList<String>();
        String s;
        boolean t;
        int p1, p2;

        for(int i = 1; i < objList.size(); i++)
        {
            GeneralObj obj = (GeneralObj) objList.elementAt(i);

            if(obj.getType() == 1) // State Transition Only
            {
                startState = ((StateTransitionObj) obj).getStartState().getName();
                endState = ((StateTransitionObj) obj).getEndState().getName();
            }
            else if(obj.getType() == 2) // Loopback Transition Only
            {
                startState = ((LoopbackTransitionObj) obj).getStartState().getName();
                endState = startState;
            }
            else
            continue;

            if(!startState.equals(stateName))
            continue;

            LinkedList<ObjAttribute> attribList = obj.getAttributeList();
            s = ("begin\n" + ind4 + nextsVar + " = " + endState + ";\n");
            for (int j = 1; j < attribList.size(); j++) {

                ObjAttribute att = attribList.get(j);

                if(j==1) // Append transition assignment
                {
                    eqn = (String) att.get(1);
                    if(eqn.equals("1"))
                    {
                        pri = "-1";
                    }
                    else
                    {
                        s = ("if(" + eqn + ") " + s);
                        pri = (String) att.get(6);
                        if(pri.equals(""))
                            pri = "0";
                    }
                }
                else // Append output assignment
                {
                    if(!att.get(1).equals(""))
                        s += (ind4 + att.get(0) + " = " + att.get(1) + ";\n");
                }
            }
            s += ind3 + "end\n";

            // Sort by priority
            // -1: lowest, else-statement
            // 0 : highest
            // 1 : second highest
            // 2 : third highest, and so on
            t = true;
            if(!eqn.equals("1"))
            for (int j = 0; j < list.size(); j += 2) {
                p1 = Integer.parseInt(pri);
                p2 = Integer.parseInt(list.get(j));

                if(p2<0 || (p1>=0 && p1<=p2))
                {
                    list.add(j, s);
                    list.add(j, pri);
                    t = false;//txt += pri + s;
                    break;
                }
            }

            if(t)
            {
                list.add(pri);
                list.add(s);//txt += pri + s;
            }

        }

        //for (int j = 0; j < list.size(); j += 1)
        for (int j = 1; j < list.size(); j += 2)
        {
            txt += ind3;
            if (j>1) txt += "else ";

            txt += list.get(j);
        }

        return txt;
    }

}