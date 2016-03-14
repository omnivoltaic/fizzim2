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
    int stateNum;
    int pageNum;
    boolean pageMode = true; // multi

    String baseStateVar = "state";
    String baseNextStateVar = "nextstate";
    String stateVar, nextStateVar;
    String holdVar = "nx_";
    LinkedList<String> onStateOut = new LinkedList<String>();
    LinkedList<String> onTransitOut = new LinkedList<String>();
    LinkedList<String> onTransitOut_dd = new LinkedList<String>();
    LinkedList<String> onStateOut_hold = new LinkedList<String>();
    LinkedList<String> onTransitOut_hold = new LinkedList<String>();
    String alwaysLine = "always @(";
    String resetLine = "";
    String resetState = "";

    String ind = "    ";
    String ind2 = ind + ind, ind3 = ind2 + ind, ind4 = ind2 + ind2;

    public GenerateHDL(String f, int p, DrawArea draw, javax.swing.JTextArea cons)
    {
        String s;
        file = new File(f);
        s = file.getName();
        modName = s.substring(0, s.length() - 2);
        pageNum = p;

        globalList = draw.globalList;
        objList = draw.objList;

        consoleText = cons;
    }

    public boolean save()
    {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String txt = "";

            Date currDate = new Date();
            long currTime = currDate.getTime();
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            DateFormat dt = DateFormat.getTimeInstance(DateFormat.MEDIUM);
            txt = ("// File last modified by Fizzim2 at " + dt.format(currTime)
                    + " on " + df.format(currDate) + "\n");
            //writer.write("version" + currVer + "\n");

            txt += "\nmodule "+ modName +" (\n";

            int stateBw;
            LinkedList<ObjAttribute> tempList;
            ObjAttribute att;
            GeneralObj obj;
            String[] ni;
            String useratts;
            int i, j;
            int t;
            String s;

            txt += "// OUTPUTS\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabOutput);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                ni = nameinfo((String) att.get(0));
                txt += (ind + "output reg" + ni[2] + " " + ni[1] + ",\n");
            }

            txt += "\n// INPUTS\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabInput);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                ni = nameinfo((String) att.get(0));
                txt += (ind + "input     " + ni[2] + " " + ni[1] + ",\n");
            }

            txt += "\n// GLOBAL\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabGlobal);

            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                s = (String) att.get(0);
                if(s.equals("clock"))
                {
                    s = (String) att.get(1);
                    txt += (ind + "input     " + s + ",\n");
                    alwaysLine += att.get(3) + " " + s;
                }
                else if (s.equals("reset_signal"))
                {
                    s = (String) att.get(1);
                    txt += (ind + "input     " + s + ",\n");
                    alwaysLine += ", " + att.get(3) + " " + s;

                    if(tempList.get(3).equals("posedge"))
                    {
                        resetLine = "if (" + s + ")";
                    } else
                    {
                        resetLine = "if (!" + s + ")";
                    }
                    alwaysLine += ")";
                }
                /*else if (s.equals("reset_state"))
                {
                    resetState = (String) att.get(1);
                }*/
                else if (s.equals("page_mode"))
                {
                    pageMode = att.get(1).equals("multi");
                }
            }
            txt += ");\n";
            txt += "\n// SIGNALS\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabSignal);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                ni = nameinfo((String) att.get(0));
                txt += ("reg " + ni[2] + " " + ni[1] + " = 0;\n");
            }

            for(int page = 1; page < pageNum; page++)
            {
                if(pageMode && pageNum > 2)
                {
                    txt += "\n//==========================\n";
                    txt += "// FSM-" + page + "\n";
                    txt += "//==========================\n";

                    stateVar = baseStateVar + "_" + page;
                    nextStateVar = baseNextStateVar + "_" + page;
                }
                else
                {
                    stateVar = baseStateVar;
                    nextStateVar = baseNextStateVar;
                }
                getOutVar(page);
                stateBw = log2(stateNum);

                txt += "\n// STATE Definitions\n";
                t = 0;
                j = 0;
                for(i = 1; i < objList.size(); i++)
                {
                    obj = (GeneralObj) objList.get(i);
                    if(pageMode && obj.getPage() != page) continue;

                    if(obj.getType() == 0) // State obj
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

                s = stateVar + ", " + nextStateVar + ";\n";
                if(stateBw > 1)
                txt += "\nreg  [" + (stateBw -1) + ":0] " + s;
                else
                txt += "\nreg  " + s;

                txt += alwaysLine + "\n";
                txt += resetLine +
                        "\n" + ind + stateVar + " <= " + resetState +
                        ";\nelse\n" + ind + stateVar + " <= " + nextStateVar + ";\n";


                LinkedList<ObjAttribute> attribList;
                txt += doTransitBlkInit();
                txt += "\n" + ind + "case (" + stateVar + ")\n";

                for(i = 1; i < objList.size(); i++)
                {
                    obj = (GeneralObj) objList.elementAt(i);
                    if(pageMode && obj.getPage() != page) continue;

                    if(obj.getType() != 0) // State Type Only
                    continue;

                    attribList = obj.getAttributeList();
                    att = attribList.get(0);
                    s = (String) att.get(1);
                    txt += ind2 + s + " :\n" + doTransit(s);
                }
                txt += ind + "endcase\nend\n";

                s = doOutputBlkInit();
                if(!s.equals(""))
                {
                    txt += s;
                    if(onStateOut.size() > 0 || onStateOut_hold.size() > 0)
                    {
                        txt += "\n" + ind + "case (" + nextStateVar + ")\n";
                        for(i = 1; i < objList.size(); i++)
                        {
                            obj = (GeneralObj) objList.elementAt(i);
                            if(pageMode && obj.getPage() != page) continue;

                            if(obj.getType() != 0) // State Type Only
                            continue;

                            attribList = obj.getAttributeList();
                            s = "";
                            for (j = attribList.size() -1; j >= 0 ; j--) {
                                att = attribList.get(j);

                                if(j==0)
                                {
                                    if(!s.equals(""))
                                    s = (ind2 + att.get(1) + " : begin\n") + s + ind2 + "end\n";
                                }
                                else if(!att.get(1).equals(""))
                                {
                                    ni = nameinfo((String) att.get(0));
                                    s += (ind3 + ni[1] + " <= " + att.get(1) + ";\n");
                                }
                            }
                            txt += s;
                        }
                        txt += ind + "endcase\n";
                    }
                    txt += "end\n";
                }

                if(!pageMode) // single mode
                    break;
            }

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
/*
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
*/
    private void getOutVar(int page)
    {
        int r=0, t=0;
        int n=0;

        onStateOut.clear();
        onTransitOut.clear();
        onTransitOut_dd.clear();
        onStateOut_hold.clear();
        onTransitOut_hold.clear();

        for(int i = 1; i < objList.size(); i++)
        {
            GeneralObj obj = (GeneralObj) objList.elementAt(i);

            if(pageMode && obj.getPage() != page) continue;

            if(obj.getType() == 0) // State Type
            {
                r = 0;
                n++;
            }
            else if(obj.getType() == 1 || obj.getType() == 2) // Transition Type
            {
                r = 2;
            }
            else
                continue;

            LinkedList<ObjAttribute> attribList = obj.getAttributeList();
            if(obj.getType() == 0 && attribList.get(0).getType().equals("reset")) // Reset State
            {
                resetState = attribList.get(0).getValue();
            }

            for (int j = 1; j < attribList.size(); j++)
            {
                ObjAttribute att = attribList.get(j);
                String name = (String) att.get(0);
                String value = (String) att.get(1);
                String type = (String) att.get(3);
                String useratts = (String) att.get(6);

                if(!value.equals("") && 
                    (type.equals("output") || type.equals("signal"))
                  )
                {
//System.out.print(name+":"+r+"=");
                    if(useratts.contains("hold"))
                        t=r+1;
                    else if(useratts.contains("-dd"))
                        t=r+2;
                    else
                        t=r;
//System.out.print(t+"\n");
                    switch(t)
                    {
                        case 0 : onStateOut.add(name); break;
                        case 1 : onStateOut_hold.add(name); break;
                        case 2 : onTransitOut.add(name); break;
                        case 3 : onTransitOut_hold.add(name); break;
                        case 4 : onTransitOut_dd.add(name);
                    }
                }
            }
        }
        stateNum = n;

    }


    private String doTransitBlkInit()
    {
        String txt = new String();
        String[] ni;
        int i;

        for (i = 0; i < onTransitOut_hold.size(); i++) {
            if(i==0)
                txt += "\n// ontransit-hold definitions\n";

            ni = nameinfo(onTransitOut_hold.get(i));
            txt += ("reg " + ni[2] + " " + holdVar + ni[1] + ";\n");
        }
        for (i = 0; i < onTransitOut_dd.size(); i++) {
            if(i==0)
                txt += "\n// ontransit-dd definitions\n";

            ni = nameinfo(onTransitOut_dd.get(i));
            txt += ("reg " + ni[2] + " " + holdVar + ni[1] + ";\n");
        }

        txt += "\n// Transition combinational always block\n";
        txt += "always @* begin\n";
        txt += ind + nextStateVar + " = " + stateVar + ";\n";

        for (i = 0; i < onTransitOut.size(); i++) {
            ni = nameinfo(onTransitOut.get(i));
            txt += (ind + ni[1] + " = 0;\n");
        }
        for (i = 0; i < onTransitOut_hold.size(); i++) {
            ni = nameinfo(onTransitOut_hold.get(i));
            txt += (ind + holdVar + ni[1] + " = " + ni[1] + ";\n");
        }
        for (i = 0; i < onTransitOut_dd.size(); i++) {
            ni = nameinfo(onTransitOut_dd.get(i));
            txt += (ind + holdVar + ni[1] + " = 0;\n");
        }

        return txt;
    }


    private String doTransit(String stateName)
    {
        String txt = new String();
        String startState = new String();
        String endState = new String();
        String eqn = "1", pri = "0";
        LinkedList<String> list = new LinkedList<String>();
        String s, useratts;
        String[] ni;
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
            s = ("begin\n" + ind4 + nextStateVar + " = " + endState + ";\n");
            for (int j = 1; j < attribList.size(); j++) {

                ObjAttribute att = attribList.get(j);

                if(j==1) // Append condition assignment
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
                else if(!att.get(1).equals("")) // Append output assignment
                {
                    ni = nameinfo((String) att.get(0));
                    useratts = (String) att.get(6);
                    if(useratts.contains("hold") || useratts.contains("-dd"))
                        s += ind4 + holdVar + ni[1];
                    else
                        s += ind4 + ni[1];

                    s += (" = " + att.get(1) + ";\n");
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


    private String doOutputBlkInit()
    {
        String txt = new String();
        String[] ni;
        int i;

        if(onStateOut.size() == 0 &&
           onStateOut_hold.size() == 0 &&
           onTransitOut_hold.size() == 0 &&
           onTransitOut_dd.size() == 0
        ) return txt;

        txt += "\n// Output sequential always block\n";
        txt += alwaysLine + "\n";
        txt += resetLine + " begin\n";

        for (i = 0; i < onTransitOut_hold.size(); i++) {
            ni = nameinfo(onTransitOut_hold.get(i));
            txt += (ind + ni[1] + " <= 0;\n");
        }
        for (i = 0; i < onTransitOut_dd.size(); i++) {
            ni = nameinfo(onTransitOut_dd.get(i));
            txt += (ind + ni[1] + " <= 0;\n");
        }
        for (i = 0; i < onStateOut.size(); i++) {
            ni = nameinfo(onStateOut.get(i));
            txt += (ind + ni[1] + " <= 0;\n");
        }
        for (i = 0; i < onStateOut_hold.size(); i++) {
            if(onTransitOut_hold.contains(onStateOut_hold.get(i))) break;

            ni = nameinfo(onStateOut_hold.get(i));
            txt += (ind + ni[1] + " <= 0;\n");
        }

        txt += "end\nelse begin\n";

        for (i = 0; i < onTransitOut_hold.size(); i++) {
            ni = nameinfo(onTransitOut_hold.get(i));
            txt += (ind + ni[1] + " <= " + holdVar + ni[1] + ";\n");
        }
        for (i = 0; i < onTransitOut_dd.size(); i++) {
            ni = nameinfo(onTransitOut_dd.get(i));
            txt += (ind + ni[1] + " <= " + holdVar + ni[1] + ";\n");
        }
        for (i = 0; i < onStateOut.size(); i++) {
            ni = nameinfo(onStateOut.get(i));
            txt += (ind + ni[1] + " <= 0;\n");
        }


        return txt;
    }


}
