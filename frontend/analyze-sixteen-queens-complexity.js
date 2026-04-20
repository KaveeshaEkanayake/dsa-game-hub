/* eslint-env node */
/* eslint-disable no-undef */

import fs from 'node:fs/promises';
import path from 'node:path';
import mysql from 'mysql2/promise';

const OUTPUT_DIR = path.resolve('test-output');

const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '2001',
  database: process.env.DB_NAME || 'dsa_game_hub'
};

function average(values) {
  if (!values.length) return 0;
  return values.reduce((sum, value) => sum + value, 0) / values.length;
}

async function loadAlgorithmRuns() {
  const connection = await mysql.createConnection(dbConfig);
  try {
    const [rows] = await connection.execute(`
      SELECT round_number, algorithm_type, logic_used, solution_count, time_taken_ms, created_at
      FROM algorithm_run
      WHERE game_name = 'SIXTEEN_QUEENS'
      ORDER BY round_number ASC, created_at ASC
    `);
    return rows;
  } finally {
    await connection.end();
  }
}

async function main() {
  await fs.mkdir(OUTPUT_DIR, { recursive: true });
  const rows = await loadAlgorithmRuns();

  if (!rows.length) {
    throw new Error('No SIXTEEN_QUEENS data found in algorithm_run table.');
  }

  const sequentialRows = rows.filter((row) => row.algorithm_type === 'SEQUENTIAL');
  const threadedRows = rows.filter((row) => row.algorithm_type === 'THREADED');

  const sequentialAvg = average(sequentialRows.map((row) => Number(row.time_taken_ms)));
  const threadedAvg = average(threadedRows.map((row) => Number(row.time_taken_ms)));
  const faster = sequentialAvg < threadedAvg ? 'SEQUENTIAL' : threadedAvg < sequentialAvg ? 'THREADED' : 'TIE';

  const report = {
    generatedAt: new Date().toISOString(),
    problemSize: '16 queens on 16x16 board',
    programLogic: {
      sequential: 'Backtracking explores row by row in a single execution path.',
      threaded: 'Backtracking work is partitioned by first-row placements and executed in parallel threads.'
    },
    theoreticalComplexity: {
      time: 'Both approaches are exponential in the search space, commonly discussed as O(N!) for N-Queens style backtracking in the worst case.',
      space: 'Board storage plus recursion depth is O(N), excluding thread-management overhead. The threaded version also adds extra worker-thread overhead.'
    },
    outputBasedAnalysis: {
      totalRuns: rows.length,
      sequentialRuns: sequentialRows.length,
      threadedRuns: threadedRows.length,
      sequentialAverageTimeMs: Number(sequentialAvg.toFixed(2)),
      threadedAverageTimeMs: Number(threadedAvg.toFixed(2)),
      fasterAlgorithmByAverage: faster,
      latestSequentialLogic: sequentialRows.at(-1)?.logic_used || 'Not found',
      latestThreadedLogic: threadedRows.at(-1)?.logic_used || 'Not found',
      commonSolutionCount: rows[0]?.solution_count || 0
    },
    conclusion: faster === 'TIE'
      ? 'Both algorithms performed similarly in the recorded runs. The threaded version still has higher coordination overhead even when timings are close.'
      : `${faster} is faster on average in the recorded outputs. Sequential has lower coordination cost, while threaded can reduce elapsed time by using parallel work distribution when the machine has enough CPU capacity.`
  };

  const textReport = `
SIXTEEN QUEENS COMPLEXITY ANALYSIS
==================================
Problem Size: ${report.problemSize}

PROGRAM LOGIC
-------------
Sequential : ${report.programLogic.sequential}
Threaded   : ${report.programLogic.threaded}

THEORETICAL COMPLEXITY
----------------------
Time Complexity : ${report.theoreticalComplexity.time}
Space Complexity: ${report.theoreticalComplexity.space}

OUTPUT-BASED ANALYSIS
---------------------
Total Runs                : ${report.outputBasedAnalysis.totalRuns}
Sequential Runs           : ${report.outputBasedAnalysis.sequentialRuns}
Threaded Runs             : ${report.outputBasedAnalysis.threadedRuns}
Sequential Average Time   : ${report.outputBasedAnalysis.sequentialAverageTimeMs} ms
Threaded Average Time     : ${report.outputBasedAnalysis.threadedAverageTimeMs} ms
Faster Algorithm Average  : ${report.outputBasedAnalysis.fasterAlgorithmByAverage}
Solution Count Observed   : ${report.outputBasedAnalysis.commonSolutionCount}
Sequential Logic Stored   : ${report.outputBasedAnalysis.latestSequentialLogic}
Threaded Logic Stored     : ${report.outputBasedAnalysis.latestThreadedLogic}

CONCLUSION
----------
${report.conclusion}
`.trimStart();

  await fs.writeFile(path.join(OUTPUT_DIR, 'sixteen-queens-complexity-report.json'), JSON.stringify(report, null, 2), 'utf8');
  await fs.writeFile(path.join(OUTPUT_DIR, 'sixteen-queens-complexity-report.txt'), textReport, 'utf8');

  console.log(textReport);
}

main().catch((error) => {
  console.error('Complexity analysis failed:', error.message);
  process.exit(1);
});
