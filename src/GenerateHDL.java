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
    String currVer;
    String modName;
    String path;
    int pageNum;
    boolean pageMode = true; // multi

    String baseStateVar = "state";
    String baseNextStateVar = "nextstate";
    String stateVar, nextStateVar;
    String holdVar = "nx_";
    LinkedList<ObjAttribute> dff_onStateOut = new LinkedList<ObjAttribute>();
    LinkedList<ObjAttribute> dff_onTransitOut = new LinkedList<ObjAttribute>();
    LinkedList<ObjAttribute> comb_onTransitOut = new LinkedList<ObjAttribute>();
    LinkedList<ObjAttribute> hold_onStateOut = new LinkedList<ObjAttribute>();
    LinkedList<ObjAttribute> hold_onTransitOut = new LinkedList<ObjAttribute>();
    LinkedList<ObjAttribute> dff_onBothOut = new LinkedList<ObjAttribute>();
    LinkedList<ObjAttribute> hold_onBothOut = new LinkedList<ObjAttribute>();
    LinkedList<ObjAttribute> bufferOut = new LinkedList<ObjAttribute>();
    LinkedList<ObjAttribute> bufferSig = new LinkedList<ObjAttribute>();
    String alwaysLine = "always @(";
    String resetLine = "";
    boolean resetSync = false;  // false for Async, true for Sync
    String resetState = "";

    String ind = "    ";
    String ind2 = ind + ind, ind3 = ind2 + ind, ind4 = ind2 + ind2;

    public GenerateHDL(String f, int p, String ver, DrawArea draw, javax.swing.JTextArea cons)
    {
        String s;
        file = new File(f);
        s = file.getName();
        modName = s.substring(0, s.length() - 2);
        pageNum = p;
        currVer = ver;

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

            txt = "// File last modified by Fizzim2 (build ";
            txt += currVer + ") at " + dt.format(currTime)
                    + " on " + df.format(currDate) + "\n";

            txt += "\nmodule "+ modName +" (\n\n";

            int stateBw;
            LinkedList<ObjAttribute> tempList;
            ObjAttribute att;
            GeneralObj obj;
            String[] ni;
            String useratts;
            int i, j;
            int t;
            String s;

            bufferOut.clear();
            //txt += "\n// OUPUTS\n";
            dff_onStateOut.clear();
            dff_onTransitOut.clear();
            comb_onTransitOut.clear();
            hold_onStateOut.clear();
            hold_onTransitOut.clear();
            dff_onBothOut.clear();
            hold_onBothOut.clear();
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabOutput);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);

                if(att.getType().equals("buffer"))
                    bufferOut.add(att);
                else if(att.getType().equals("dff-onstate"))
                    dff_onStateOut.add(att);
                else if(att.getType().equals("dff-ontransit"))
                    dff_onTransitOut.add(att);
                else if(att.getType().equals("hold-onstate"))
                    hold_onStateOut.add(att);
                else if(att.getType().equals("hold-ontransit"))
                    hold_onTransitOut.add(att);
                else if(att.getType().equals("comb-ontransit"))
                    comb_onTransitOut.add(att);
                else if(att.getType().equals("dff-onboth"))
                    dff_onBothOut.add(att);
                else if(att.getType().equals("hold-onboth"))
                    hold_onBothOut.add(att);
            }
            txt += doOutputDefinition(0);

            txt += "// INPUTS\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabInput);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                ni = nameinfo(att);
                txt += (ind + "input      " + ni[2] + " " + ni[1] + ",\n");
            }

            txt += "\n// GLOBAL\n";
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabGlobal);

            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);
                s = (String) att.get(0);
                if(s.equals("clock"))
                {
                    s = (String) att.get(1);
                    txt += (ind + "input " + ind3 + s);
                    alwaysLine += att.get(3) + " " + s;
                }
                else if (s.equals("reset_signal"))
                {
                    s = (String) att.get(1);
                    if(!att.getType().equals("sync"))
                        txt += (",\n" + ind + "input " + ind3 + s);

                    resetSync = false;
                    if(att.get(3).equals("posedge"))
                    {
                        resetLine = "if (" + s + ")";
                        alwaysLine += ", " + att.get(3) + " " + s;
                    } else if(att.get(3).equals("negedge"))
                    {
                        resetLine = "if (!" + s + ")";
                        alwaysLine += ", " + att.get(3) + " " + s;
                    } else
                    {
                        resetLine = "if (" + s + ")";
                        resetSync = true;
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
            txt += "\n);\n\n";
            bufferSig.clear();
            //txt += "\n// SIGNALS\n";
            dff_onStateOut.clear();
            dff_onTransitOut.clear();
            comb_onTransitOut.clear();
            hold_onStateOut.clear();
            hold_onTransitOut.clear();
            dff_onBothOut.clear();
            hold_onBothOut.clear();
            tempList = (LinkedList<ObjAttribute>) globalList.get(ObjAttribute.TabSignal);
            for (i = 0; i < tempList.size(); i++) {
                att = tempList.get(i);

                if(att.getType().equals("buffer"))
                    bufferSig.add(att);
                else if(att.getType().equals("dff-onstate"))
                    dff_onStateOut.add(att);
                else if(att.getType().equals("dff-ontransit"))
                    dff_onTransitOut.add(att);
                else if(att.getType().equals("hold-onstate"))
                    hold_onStateOut.add(att);
                else if(att.getType().equals("hold-ontransit"))
                    hold_onTransitOut.add(att);
                else if(att.getType().equals("comb-ontransit"))
                    comb_onTransitOut.add(att);
                else if(att.getType().equals("dff-onboth"))
                    dff_onBothOut.add(att);
                else if(att.getType().equals("hold-onboth"))
                    hold_onBothOut.add(att);
            }
            txt += doOutputDefinition(1);

            for(int page = 1; page < pageNum; page++)
            {
                if(pageMode && pageNum > 2)
                {
                    txt += "//==========================\n";
                    txt += "// FSM-" + page + "\n";
                    txt += "//==========================\n\n";

                    stateVar = baseStateVar + "_" + page;
                    nextStateVar = baseNextStateVar + "_" + page;
                }
                else
                {
                    stateVar = baseStateVar;
                    nextStateVar = baseNextStateVar;
                }
                stateBw = log2(getStateNum(page));

                txt += "// STATE Definitions\n";
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
                    if(dff_onStateOut.size() > 0 || hold_onStateOut.size() > 0)
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
                                    ni = nameinfo(att);
                                    s += (ind3 + ni[1] + " <= " + att.get(1) + ";\n");
                                }
                            }
                            txt += s;
                        }
                        txt += ind + "endcase\n";
                    }
                    txt += "end\n";
                }

                txt += doSimBlk(page);
                if(!pageMode) // single mode
                    break;
            }

            txt += "endmodule // Fizzim2\n";
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

    public String[] nameinfo(ObjAttribute att)
    {
        String s = att.getName().replace(" ", "");
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
        String msb=null, lsb=null, bus="      ", width="1", resetVar=att.getresetval();
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

                if(msb.length() + lsb.length() < 3) // both msb and lsb are 1-digital
                    bus = " " + bus;

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

        if(resetVar == null || resetVar.length() == 0)
            resetVar = width+"'d0";

        //System.out.println("-"+s+"-"+msb+"-"+lsb+"-"+width+"-"+resetVar);
        //System.out.println(log);

        return new String[]{log, s, bus, width, msb, lsb, order, resetVar};
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
    private int getStateNum(int page)
    {
        int n=0;
        boolean t;
        LinkedList<ObjAttribute> ll = dff_onStateOut;

        dff_onStateOut.clear();
        dff_onTransitOut.clear();
        comb_onTransitOut.clear();
        hold_onStateOut.clear();
        hold_onTransitOut.clear();
        dff_onBothOut.clear();
        hold_onBothOut.clear();

        for(int i = 1; i < objList.size(); i++)
        {
            GeneralObj obj = (GeneralObj) objList.elementAt(i);

            if(pageMode && obj.getPage() != page) continue;

            if(obj.getType() == 0) // State Type
            {
                n++;
            }
            else if(obj.getType() == 1 || obj.getType() == 2) // Transition Type
            {
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
                    if(useratts.contains("dff-onstate"))
                        ll = dff_onStateOut;
                    else if(useratts.contains("dff-ontransit"))
                        ll = dff_onTransitOut;
                    else if(useratts.contains("comb-ontransit"))
                        ll = comb_onTransitOut;
                    else if(useratts.contains("hold-onstate"))
                        ll = hold_onStateOut;
                    else if(useratts.contains("hold-ontransit"))
                        ll = hold_onTransitOut;
                    else if(useratts.contains("dff-onboth"))
                        ll = dff_onBothOut;
                    else if(useratts.contains("hold-onboth"))
                        ll = hold_onBothOut;
//System.out.print(t+"\n");
                    t = true;
                    for (int k = 0; k < ll.size(); k++)
                    {
                        if(name.equals((String) ll.get(k).get(0)))
                        {
                            t = false;
                            break;
                        }
                    }
                    if(t) ll.add(att);
                }
            }
        }
        return n;

    }


    private String doTransitBlkInit()
    {
        String txt = new String();
        String[] ni;
        int i;

        if(dff_onTransitOut.size() + dff_onBothOut.size() > 0)
            txt += "\n// dff-onTransit definitions\n";
        for (i = 0; i < dff_onTransitOut.size(); i++) {
            ni = nameinfo(dff_onTransitOut.get(i));
            txt += ("reg " + ni[2] + " " + holdVar + ni[1] + " = " + ni[7] + ";\n");
        }
        for (i = 0; i < dff_onBothOut.size(); i++) {
            ni = nameinfo(dff_onBothOut.get(i));
            txt += ("reg " + ni[2] + " " + holdVar + ni[1] + " = " + ni[7] + ";\n");
        }

        if(hold_onTransitOut.size() + hold_onBothOut.size() > 0)
            txt += "\n// hold-onTransit definitions\n";
        for (i = 0; i < hold_onTransitOut.size(); i++) {
            ni = nameinfo(hold_onTransitOut.get(i));
            txt += ("reg " + ni[2] + " " + holdVar + ni[1] + " = " + ni[7] + ";\n");
        }
        for (i = 0; i < hold_onBothOut.size(); i++) {
            ni = nameinfo(hold_onBothOut.get(i));
            txt += ("reg " + ni[2] + " " + holdVar + ni[1] + " = " + ni[7] + ";\n");
        }

        txt += "\n// Transition combinational always block\n";
        txt += "always @* begin\n";
        txt += ind + nextStateVar + " = " + stateVar + ";\n";

        for (i = 0; i < dff_onTransitOut.size(); i++) {
            ni = nameinfo(dff_onTransitOut.get(i));
            txt += (ind + holdVar + ni[1] + " = " + ni[7] + ";\n");
        }
        for (i = 0; i < dff_onBothOut.size(); i++) {
            ni = nameinfo(dff_onBothOut.get(i));
            txt += (ind + holdVar + ni[1] + " = " + ni[7] + ";\n");
        }
        for (i = 0; i < hold_onTransitOut.size(); i++) {
            ni = nameinfo(hold_onTransitOut.get(i));
            txt += (ind + holdVar + ni[1] + " = " + ni[1] + ";\n");
        }
        for (i = 0; i < hold_onBothOut.size(); i++) {
            ni = nameinfo(hold_onBothOut.get(i));
            txt += (ind + holdVar + ni[1] + " = " + ni[1] + ";\n");
        }
        for (i = 0; i < comb_onTransitOut.size(); i++) {
            ni = nameinfo(comb_onTransitOut.get(i));
            txt += (ind + ni[1] + " = " + ni[7] + ";\n");
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

            if(obj.getType() == 1) // Transition Only
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
                    ni = nameinfo(att);
                    useratts = (String) att.get(6);
                    if(useratts.contains("hold-") || useratts.contains("dff-"))
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

        if(dff_onStateOut.size() == 0 &&
           hold_onStateOut.size() == 0 &&
           hold_onTransitOut.size() == 0 &&
           bufferOut.size() == 0 &&
           bufferSig.size() == 0 &&
           dff_onTransitOut.size() == 0
        ) return txt;

        txt += "\n// Output sequential always block\n";
        txt += alwaysLine + "\n";

        if(!resetSync)
        {
        txt += resetLine + " begin\n";

        for (i = 0; i < bufferOut.size(); i++) {
            ni = nameinfo(bufferOut.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }
        for (i = 0; i < bufferSig.size(); i++) {
            ni = nameinfo(bufferSig.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }
        for (i = 0; i < dff_onTransitOut.size(); i++) {
            ni = nameinfo(dff_onTransitOut.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }
        for (i = 0; i < dff_onStateOut.size(); i++) {
            ni = nameinfo(dff_onStateOut.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }
        for (i = 0; i < dff_onBothOut.size(); i++) {
            ni = nameinfo(dff_onBothOut.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }
        for (i = 0; i < hold_onTransitOut.size(); i++) {
            ni = nameinfo(hold_onTransitOut.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }
        for (i = 0; i < hold_onStateOut.size(); i++) {
            ni = nameinfo(hold_onStateOut.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }
        for (i = 0; i < hold_onBothOut.size(); i++) {
            ni = nameinfo(hold_onBothOut.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }

        txt += "end\nelse ";
        }
        txt += "begin\n";

        for (i = 0; i < bufferOut.size(); i++) {
            ni = nameinfo(bufferOut.get(i));
            txt += (ind + ni[1] + " <= " + bufferOut.get(i).getUserAtts() + ";\n");
        }
        for (i = 0; i < bufferSig.size(); i++) {
            ni = nameinfo(bufferSig.get(i));
            txt += (ind + ni[1] + " <= " + bufferSig.get(i).getUserAtts() + ";\n");
        }
        for (i = 0; i < dff_onTransitOut.size(); i++) {
            ni = nameinfo(dff_onTransitOut.get(i));
            txt += (ind + ni[1] + " <= " + holdVar + ni[1] + ";\n");
        }
        for (i = 0; i < dff_onBothOut.size(); i++) {
            ni = nameinfo(dff_onBothOut.get(i));
            txt += (ind + ni[1] + " <= " + holdVar + ni[1] + ";\n");
        }
        for (i = 0; i < hold_onTransitOut.size(); i++) {
            ni = nameinfo(hold_onTransitOut.get(i));
            txt += (ind + ni[1] + " <= " + holdVar + ni[1] + ";\n");
        }
        for (i = 0; i < hold_onBothOut.size(); i++) {
            ni = nameinfo(hold_onBothOut.get(i));
            txt += (ind + ni[1] + " <= " + holdVar + ni[1] + ";\n");
        }
        for (i = 0; i < dff_onStateOut.size(); i++) {
            ni = nameinfo(dff_onStateOut.get(i));
            txt += (ind + ni[1] + " <= " + ni[7] + ";\n");
        }

        return txt;
    }


    private String doSimBlk(int page)
    {
        String txt = "\n// This code allows you to see state names in simulation\n";
        String stateSim = stateVar + "_name";
        String s = new String();

        txt += "`ifndef SYNTHESIS\nreg [31:0] ";
        txt += stateSim + ";\nalways @* begin\n";
        txt += ind + "case (" + stateVar + ")\n";
        for(int i = 1; i < objList.size(); i++)
        {
            GeneralObj obj = (GeneralObj) objList.elementAt(i);
            if(pageMode && obj.getPage() != page)
                continue;
            if(obj.getType() != 0) // State Only
                continue;

            //LinkedList<ObjAttribute> attribList = obj.getAttributeList();
            ObjAttribute attrib = obj.getAttributeList().get(0);
            s = attrib.getValue();
            txt += ind2 + s + " : " + stateSim + " = \"" + s + "\";\n";
        }

        txt += ind2 + "default : " + stateSim + " = \"XXX\";\n";
        txt += ind + "endcase\nend\n`endif\n\n";

        return txt;
    }


    private String doOutputDefinition(int t)
    {
        String s1 = "// SIGNALS ";
        String s2 = "reg ";
        String s3 = ",\n";
        String[] ni;
        String txt = "";

        if(t == 0) // for Outputs
        {
            s1 = "// OUTPUTS ";
            s2 = ind + "output " + s2;
        }

        if(dff_onStateOut.size() > 0)
            txt += s1 + "dff-onState\n";
        for (int i = 0; i < dff_onStateOut.size(); i++) {
            ni = nameinfo(dff_onStateOut.get(i));
            if(t != 0) // for Signals
                s3 = " = " + ni[7] + ";\n";

            txt += (s2 + ni[2] + " " + ni[1] + s3);
        }
        if(dff_onTransitOut.size() > 0)
            txt += s1 + "dff-onTransit\n";
        for (int i = 0; i < dff_onTransitOut.size(); i++) {
            ni = nameinfo(dff_onTransitOut.get(i));
            if(t != 0)
                s3 = " = " + ni[7] + ";\n";

            txt += (s2 + ni[2] + " " + ni[1] + s3);
        }
        if(comb_onTransitOut.size() > 0)
            txt += s1 + "comb-onTransit\n";
        for (int i = 0; i < comb_onTransitOut.size(); i++) {
            ni = nameinfo(comb_onTransitOut.get(i));
            if(t != 0)
                s3 = " = " + ni[7] + ";\n";

            txt += (s2 + ni[2] + " " + ni[1] + s3);
        }
        if(hold_onStateOut.size() > 0)
            txt += s1 + "hold-onState\n";
        for (int i = 0; i < hold_onStateOut.size(); i++) {
            ni = nameinfo(hold_onStateOut.get(i));
            if(t != 0)
                s3 = " = " + ni[7] + ";\n";

            txt += (s2 + ni[2] + " " + ni[1] + s3);
        }
        if(hold_onTransitOut.size() > 0)
            txt += s1 + "hold-onTransit\n";
        for (int i = 0; i < hold_onTransitOut.size(); i++) {
            ni = nameinfo(hold_onTransitOut.get(i));
            if(t != 0)
                s3 = " = " + ni[7] + ";\n";

            txt += (s2 + ni[2] + " " + ni[1] + s3);
        }
        if(dff_onBothOut.size() > 0)
            txt += s1 + "dff-onBoth\n";
        for (int i = 0; i < dff_onBothOut.size(); i++) {
            ni = nameinfo(dff_onBothOut.get(i));
            if(t != 0)
                s3 = " = " + ni[7] + ";\n";

            txt += (s2 + ni[2] + " " + ni[1] + s3);
        }
        if(hold_onBothOut.size() > 0)
            txt += s1 + "hold-onBoth\n";
        for (int i = 0; i < hold_onBothOut.size(); i++) {
            ni = nameinfo(hold_onBothOut.get(i));
            if(t != 0)
                s3 = " = " + ni[7] + ";\n";

            txt += (s2 + ni[2] + " " + ni[1] + s3);
        }

        if(t == 0) // for Outputs buffer
        {
            if(bufferOut.size() > 0)
                txt += s1 + "buffer\n";
            for (int i = 0; i < bufferOut.size(); i++) {
                ni = nameinfo(bufferOut.get(i));

                txt += (s2 + ni[2] + " " + ni[1] + s3);
            }
        } else { // for Signals buffer
            if(bufferSig.size() > 0)
                txt += s1 + "buffer\n";
            for (int i = 0; i < bufferSig.size(); i++) {
                ni = nameinfo(bufferSig.get(i));
                s3 = " = " + ni[7] + ";\n";
                txt += (s2 + ni[2] + " " + ni[1] + s3);
            }
        }

        if(txt.equals(""))
            return txt;
        else
            return txt + "\n";
    }

// end of class GenerateHDL
}
