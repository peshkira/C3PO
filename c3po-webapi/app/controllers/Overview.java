/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.petpet.c3po.analysis.ConflictResolutionProcessor;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.utils.Configurator;
import helpers.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.overview;

import com.petpet.c3po.api.model.helper.Filter;

public class Overview extends Controller {
    public static GraphData getAllGraphs() {
        return allGraphs;
    }

    static GraphData allGraphs=new GraphData();
    public static Result index() {
        Application.buildSession();
        Logger.debug("Received an index call in overview");
        final List<String> names = Properties.getCollectionNames();
        List<Graph> graphs = new ArrayList<Graph>();
        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();

        Filter filter = Filters.getFilterFromSession();

        List<FilterCondition> conditions = filter.getConditions();
        String fieldTail=null;
        Iterator<FilterCondition> iterator1 = conditions.iterator();
        while (iterator1.hasNext()){
            FilterCondition condition = iterator1.next();
            if (condition.getValue()!=null && condition.getValue().equals("NOLONGTAIL")){
                fieldTail = condition.getField();
                iterator1.remove();
                break;
            }
        }


        TemplatesLoader.setProps(filter);

        ArrayList<String> properties = Lists.newArrayList(Application.PROPS);
        properties.add("size");
        Map<String, Distribution> distributions = Properties.getDistributions(properties, filter);
        Long conflictsCount= crp.getConflictsCount(filter);
        Iterator<Map.Entry<String, Distribution>> iterator = distributions.entrySet().iterator();



        StatisticsToPrint stats = new StatisticsToPrint();
        while (iterator.hasNext()){
            Map.Entry<String, Distribution> next = iterator.next();
            if (next.getKey().equals("size")){
                Distribution sizeDistribution = next.getValue();
                if (sizeDistribution.getPropertyValues().size()==0)
                    continue;
                stats.setAvg(Properties.round(sizeDistribution.getValue("avg") / 1024.0 / 1024.0, 3) + " MB");
                stats.setCount(sizeDistribution.getValue("count").intValue() + " objects");
                stats.setMax(Properties.round(sizeDistribution.getValue("max") / 1024.0 / 1024.0, 3) + " MB");
                stats.setMin(Properties.round(sizeDistribution.getValue("min") / 1024.0 / 1024.0, 3) + " MB");
                stats.setSd(Properties.round(sizeDistribution.getValue("std") / 1024.0 / 1024.0, 3) + " MB");
                stats.setSize(Properties.round(sizeDistribution.getValue("sum") / 1024.0 / 1024.0, 3) + " MB");
                stats.setVar(Properties.round(sizeDistribution.getValue("var") / 1024.0 / 1024.0, 3) + " MB^2");
                stats.setConflicts(Properties.round(100.0*conflictsCount/sizeDistribution.getValue("count").intValue(),3)+ "%" + " (" +  conflictsCount  + " objects)");

            } else {
                Graph g = Properties.interpretDistribution(next.getValue(), null, null);
                if (!next.getKey().equals(fieldTail))
                    g.cutLongTail();
                graphs.add(g);


            }


        }

        allGraphs = new GraphData(graphs);



        return ok(overview.render(names, allGraphs, stats, TemplatesLoader.getCurrentTemplate()));
    }

    public static void addToAllGraphs(Graph g){
        List<Graph> graphs = allGraphs.getGraphs();
        Iterator<Graph> iterator = graphs.iterator();
        while (iterator.hasNext()){
            Graph next = iterator.next();
            if (next.getProperty().equals(g.getProperty())){
                if (next.getFilter()!=null && g.getFilter()!=null && !next.getFilter().equals(g.getFilter())){
                    next=g;
                    return;
                }
            }
        }
        graphs.add(g);
    }

    public static Result addGraph(String property) {
        Logger.debug("Received a addGraph call for property '" + property + "'");

        // if it is one of the default properties, do not draw..
        for (String p : Application.PROPS) {
            if (p.equals(property)) {
                return ok();
            }
        }
        DynamicForm form = play.data.Form.form().bindFromRequest();
        String alg = form.get("alg");
        String width = form.get("width");
        if (width!=null && width.equals("-1"))
            width=null;
        Filter filter = Filters.getFilterFromSession();
        Distribution d = Properties.getDistribution(property, filter);
        Graph g = Properties.interpretDistribution(d,alg,width);
        g.cutLongTail();
        allGraphs.getGraphs().add(g);
        TemplatesLoader.addUserDefinedGraph(property);
        return ok(play.libs.Json.toJson(g));
    }

    public static Result indexFiltered() {
        Http.RequestBody body = request().body();
        String path = request().path();
        String uri = request().uri();
        uri=uri.replace(path+"?","").replace("&template=Conflict","");
        Filter f= null;
        try {
            f = new Filter(uri);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        /*for (Map.Entry<String, String[]> stringEntry : stringMap.entrySet()) {
            String key = stringEntry.getKey();
            String[] value = stringEntry.getValue();
            if (key.equals("template"))
                templateName=value[0];
            else
                f.addFilterCondition(new FilterCondition(key,value));
        }
*/
        Filters.setFilterFromSession(f);
        TemplatesLoader.setCurrentTemplateName("Conflict");
        return redirect("/c3po/overview");
    }


    public static Result resetTemplate() {
        TemplatesLoader.resetTemplates();
        return redirect("/c3po/overview");
    }
}
