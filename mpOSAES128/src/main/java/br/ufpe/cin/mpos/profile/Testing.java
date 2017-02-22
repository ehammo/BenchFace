/*******************************************************************************
 * Copyright (C) 2014 Philipp B. Costa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.ufpe.cin.mpos.profile;

import android.database.Cursor;

import java.util.TimerTask;

import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufpe.cin.mpos.DaoLocal.DatabaseController;
import br.ufpe.cin.mpos.profile.Model.Model;

/**
 * This implementation made decision about local or remote execution,
 * based in profile results (ping tcp).
 * 
 * @author Philipp B. Costa
 */
public class Testing extends TimerTask {
    private final String clsName = Testing.class.getName();

    private final Object mutex = new Object();
    private final long PING_TOLERANCE = 50;//ms

    private ServerContent server;
    // private ProfileNetworkDAO profileDao;

    DatabaseController dc;

    public Testing(DatabaseController dc) {
        this.dc = dc;
    }

    public Model createModel(Cursor c){
        Model m = new Model();
        c.moveToFirst();
        m.Tech = c.getString(1);
        m.AppName = c.getString(2);
        m.Carrier = c.getString(3);
        m.Battery = c.getString(4);
        m.year = c.getString(5);
        m.CPU = c.getString(6);
        m.SizeInput = c.getString(7);
        m.Bandwidth = c.getString(8);
        m.RSSI = c.getString(10);
        m.Date = c.getString(11);
        m.CPUNuvem = c.getString(12);
        return m;
    }

    @Override
    public void run() {
        Cursor c = dc.getData();
        c.moveToFirst();
        int id = Integer.parseInt(c.getString(0));
        for (int i=0;i<c.getColumnCount()-1;i++){
            c.moveToFirst();
            if(i==(c.getColumnCount()-2)&&id>=40){
                Model model = createModel(c);
                dc.dm.onUpgrade(dc.db,1,1);
                dc.insertData(model);
            }
        }

        dc.db.close();
        c.close();
    }
}