/**
 * Copyright (c) 2011-2013, kidzhou 周磊 (zhouleib1412@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dbumama.market.web.core.render.chart.amchart;

import com.dbumama.market.web.core.render.KeyLabel;

import java.util.List;

public class PieChart {

    private List<KeyLabel> pies;

    public PieChart() {

    }

    public PieChart(List<KeyLabel> pies) {
        this.pies = pies;
    }

    public List<KeyLabel> getPies() {
        return pies;
    }

    public void setPies(List<KeyLabel> pies) {
        this.pies = pies;
    }

}
