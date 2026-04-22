/* eslint-env node */
/* eslint-disable no-undef */

import fs from "node:fs/promises";
import path from "node:path";
import mysql from "mysql2/promise";
import { ChartJSNodeCanvas } from "chartjs-node-canvas";

const OUTPUT_DIR = path.resolve("test-output");
const WIDTH = 1200;
const HEIGHT = 700;

const dbConfig = {
  host: process.env.DB_HOST || "localhost",
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || "root",
  password: process.env.DB_PASSWORD || "2001",
  database: process.env.DB_NAME || "dsa_game_hub",
};

const valueLabelPlugin = {
  id: "valueLabelPlugin",
  afterDatasetsDraw(chart) {
    const { ctx } = chart;
    ctx.save();
    ctx.font = "bold 12px Arial";
    ctx.fillStyle = "#111";
    ctx.textAlign = "center";
    ctx.textBaseline = "bottom";

    chart.data.datasets.forEach((dataset, datasetIndex) => {
      const meta = chart.getDatasetMeta(datasetIndex);

      meta.data.forEach((bar, index) => {
        const value = dataset.data[index];
        if (value === null || value === undefined) return;

        ctx.fillText(String(value), bar.x, bar.y - 6);
      });
    });

    ctx.restore();
  },
};

const chartCanvas = new ChartJSNodeCanvas({
  width: WIDTH,
  height: HEIGHT,
  backgroundColour: "white",
  chartCallback: (ChartJS) => {
    ChartJS.register(valueLabelPlugin);
  },
});

async function loadRoundData() {
  const conn = await mysql.createConnection(dbConfig);
  try {
    const [rows] = await conn.execute(`
      SELECT round_number, sequential_time_taken_ns, threaded_time_taken_ns
      FROM game_round
      WHERE game_name = 'SIXTEEN_QUEENS'
      ORDER BY round_number ASC
    `);
    return rows;
  } finally {
    await conn.end();
  }
}

async function loadAlgorithmRunData() {
  const conn = await mysql.createConnection(dbConfig);
  try {
    const [rows] = await conn.execute(`
      SELECT id, round_number, algorithm_type, time_taken_ms
      FROM algorithm_run
      WHERE game_name = 'SIXTEEN_QUEENS'
      ORDER BY round_number ASC, id ASC
    `);
    return rows;
  } finally {
    await conn.end();
  }
}

async function saveRoundChart(rows) {
  const labels = rows.map((r) => `R${r.round_number}`);
  const sequential = rows.map((r) => Number(r.sequential_time_taken_ns || 0));
  const threaded = rows.map((r) => Number(r.threaded_time_taken_ns || 0));

  const config = {
    type: "bar",
    data: {
      labels,
      datasets: [
        {
          label: "Sequential (ns)",
          data: sequential,
        },
        {
          label: "Threaded (ns)",
          data: threaded,
        },
      ],
    },
    options: {
      responsive: false,
      layout: {
        padding: {
          top: 30,
        },
      },
      plugins: {
        title: {
          display: true,
          text: "Sixteen Queens - Round Time Chart",
        },
        legend: {
          display: true,
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: "Time (ns)",
          },
        },
        x: {
          title: {
            display: true,
            text: "Rounds",
          },
        },
      },
    },
  };

  const buffer = await chartCanvas.renderToBuffer(config);
  await fs.writeFile(path.join(OUTPUT_DIR, "round-chart.png"), buffer);
}

async function saveAlgorithmChart(rows) {
  const labels = rows.map((r) => `${r.algorithm_type}-R${r.round_number}`);
  const times = rows.map((r) => Number(r.time_taken_ms || 0));

  const config = {
    type: "bar",
    data: {
      labels,
      datasets: [
        {
          label: "Time (ms)",
          data: times,
        },
      ],
    },
    options: {
      responsive: false,
      layout: {
        padding: {
          top: 30,
        },
      },
      plugins: {
        title: {
          display: true,
          text: "Sixteen Queens - Algorithm Run Chart",
        },
        legend: {
          display: true,
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: "Time (ms)",
          },
        },
        x: {
          title: {
            display: true,
            text: "Algorithm Runs",
          },
        },
      },
    },
  };

  const buffer = await chartCanvas.renderToBuffer(config);
  await fs.writeFile(path.join(OUTPUT_DIR, "algorithm-chart.png"), buffer);
}

function printRoundTable(rows) {
  console.log("\n=== ROUND TABLE ===");
  console.table(
    rows.map((r) => ({
      round_number: r.round_number,
      sequential_time_taken_ns: Number(r.sequential_time_taken_ns || 0),
      threaded_time_taken_ns: Number(r.threaded_time_taken_ns || 0),
    })),
  );
}

function printAlgorithmTable(rows) {
  console.log("\n=== ALGORITHM RUN TABLE ===");
  console.table(
    rows.map((r) => ({
      id: r.id,
      round_number: r.round_number,
      algorithm_type: r.algorithm_type,
      time_taken_ms: Number(r.time_taken_ms || 0),
    })),
  );
}

async function main() {
  await fs.mkdir(OUTPUT_DIR, { recursive: true });

  const roundData = await loadRoundData();
  const algoData = await loadAlgorithmRunData();

  if (!roundData.length) {
    throw new Error("No round data found");
  }

  if (!algoData.length) {
    throw new Error("No algorithm data found");
  }

  await saveRoundChart(roundData);
  await saveAlgorithmChart(algoData);

  printRoundTable(roundData);
  printAlgorithmTable(algoData);

  console.log("✔ Chart images generated in test-output/");
  console.log("✔ Tables printed in console");
}

main().catch((err) => {
  console.error("Error:", err.message);
  process.exit(1);
});