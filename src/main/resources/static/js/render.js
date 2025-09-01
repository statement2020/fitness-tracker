function initFitnessCharts(users, userEntriesMap) {
    const traces = [];
    const weightTraces = [];
    const traceIndexMap = {}; // user.id → trace indexes
    let caloriesTraceCounter = 0;
    let weightTraceCounter = 0;

// Build initial traces from saved entries
    users.forEach(user => {
        const entries = userEntriesMap[user.id].sort((a, b) => new Date(a.date) - new Date(b.date)) || [];
        const dates = entries.map(e => e.date.split('T')[0]);

        traceIndexMap[user.id] = {};

        // Calories Burnt
        traceIndexMap[user.id].burnt = caloriesTraceCounter;
        traces.push({
            x: dates,
            y: entries.map(e => e.caloriesBurnt),
            type: 'bar',
            name: user.name + ' - Burnt'
        });
        caloriesTraceCounter++;

        // Calories Consumed
        traceIndexMap[user.id].consumed = caloriesTraceCounter;
        traces.push({
            x: dates,
            y: entries.map(e => e.caloriesConsumed),
            type: 'bar',
            name: user.name + ' - Consumed'
        });
        caloriesTraceCounter++;

        // %BMR Consumed
        traceIndexMap[user.id].pctConsumed = caloriesTraceCounter;
        traces.push({
            x: dates,
            y: entries.map(e => (e.caloriesConsumed / e.bmr) * 100),
            type: 'scatter',
            mode: 'lines+markers',
            name: user.name + ' - %BMR Consumed',
            yaxis: 'y2'
        });
        caloriesTraceCounter++;

        // %BMR Burnt
        traceIndexMap[user.id].pctBurnt = caloriesTraceCounter;
        traces.push({
            x: dates,
            y: entries.map(e => (e.caloriesBurnt / e.bmr) * 100),
            type: 'scatter',
            mode: 'lines+markers',
            name: user.name + ' - %BMR Burnt',
            yaxis: 'y2'
        });
        caloriesTraceCounter++;

        // BMR
        traceIndexMap[user.id].bmr = caloriesTraceCounter;
        traces.push({
            x: dates,
            y: entries.map(e => e.bmr),
            type: 'scatter',
            mode: 'lines+markers',
            name: user.name + ' - BMR',
            yaxis: 'y2'
        });
        caloriesTraceCounter++;

        // Weight trace
        traceIndexMap[user.id].weight = weightTraceCounter;
        weightTraces.push({
            x: dates,
            y: entries.map(e => e.weight),
            type: 'scatter',
            mode: 'lines+markers',
            name: user.name
        });
        weightTraceCounter++;
    });

// Layouts
    const caloriesLayout = {
        barmode: 'group',
        title: 'Calories & %BMR Over Time',
        xaxis: {title: 'Date', type: 'date'},
        yaxis: {title: 'Calories'},
        yaxis2: {
            title: '% of BMR',
            overlaying: 'y',
            side: 'right'
        },
        legend: {orientation: "h", y: -0.3},
        margin: {t: 50, b: 100}
    };

    const weightLayout = {
        title: 'Weight Over Time',
        xaxis: {title: 'Date', type: 'date'},
        yaxis: {title: 'Weight'},
        legend: {orientation: "h", y: -0.3},
        margin: {t: 50, b: 100}
    };

// Render initial charts
    Plotly.newPlot('caloriesChart', traces, caloriesLayout);
    Plotly.newPlot('weightChart', weightTraces, weightLayout);

// --- SSE for real-time updates ---
    const eventSource = new EventSource('/stream/entries');

    eventSource.addEventListener("new-entry", function (event) {
        const entry = JSON.parse(event.data);
        const dateOnly = entry.date.split("T")[0];
        const consumedPct = (entry.caloriesConsumed / entry.bmr) * 100;
        const burntPct = (entry.caloriesBurnt / entry.bmr) * 100;

        const idx = traceIndexMap[entry.user.id];
        if (!idx) return;

        // Calories chart updates
        Plotly.extendTraces('caloriesChart',
            {x: [[dateOnly]], y: [[entry.caloriesBurnt]]},
            [idx.burnt]
        );
        Plotly.extendTraces('caloriesChart',
            {x: [[dateOnly]], y: [[entry.caloriesConsumed]]},
            [idx.consumed]
        );
        Plotly.extendTraces('caloriesChart',
            {x: [[dateOnly]], y: [[consumedPct]]},
            [idx.pctConsumed]
        );
        Plotly.extendTraces('caloriesChart',
            {x: [[dateOnly]], y: [[burntPct]]},
            [idx.pctBurnt]
        );

        // Weight chart update
        Plotly.extendTraces('weightChart',
            {x: [[dateOnly]], y: [[entry.weight]]},
            [idx.weight]
        );
    });
}