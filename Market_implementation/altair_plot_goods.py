import sys
import altair as alt
import numpy as np
import pandas as pd

alt.renderers.enable('default')

good_data = sys.argv[1].split(",")
good_id = sys.argv[2]
price_data = []
round_data = []

for i in good_data:
    split_data = i.split("=")
    round_data.append(int(split_data[0]))
    price_data.append(float(split_data[1]))

max_price = max(price_data)
max_round = max(round_data)

source = pd.DataFrame({
  'price': price_data,
  'rounds': round_data
})

chart = alt.Chart(source).mark_line().encode(
    alt.X('rounds:Q',
          scale=alt.Scale(domain=(min(round_data), max(round_data)), type='ordinal')),
    alt.Y('price:Q',
          scale=alt.Scale(domain=(0, max_price),))
).properties(
    width=1000,
    height=800
)

filename = "chart_good_" + good_id + ".html"
chart.save('charts/goods/' + filename)
