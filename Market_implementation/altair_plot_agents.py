import sys
import altair as alt
import numpy as np
import pandas as pd

alt.renderers.enable('default')

agent_data = sys.argv[1].split(",")
agent_id = sys.argv[2]
fund_data = []
round_data = []

for i in agent_data:
    split_data = i.split("=")
    round_data.append(int(split_data[0]))
    fund_data.append(float(split_data[1]))

max_funds = max(fund_data)
max_round = max(round_data)

source = pd.DataFrame({
  'funds': fund_data,
  'rounds': round_data
})

chart = alt.Chart(source).mark_line().encode(
    alt.X('rounds:Q',
          scale=alt.Scale(domain=(min(round_data), max(round_data)), type='ordinal')),
    alt.Y('funds:Q',
          scale=alt.Scale(domain=(0, max_funds),))
).properties(
    width=1000,
    height=800
)

filename = "chart_agent_" + agent_id + ".html"
chart.save('charts/agents/' + filename)
