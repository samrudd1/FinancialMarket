import sys
import altair as alt
import numpy as np
import pandas as pd
import jupyter as jp
import matplotlib.pyplot as plt
#import altair_viewer as av
#import altair_saver as asvr

#alt.renderers.enable('default')

#good_data = sys.argv[1].split(",")
#good_id = sys.argv[2]
#price_data = []
#round_data = []

#for i in good_data:
#    split_data = i.split("=")
#    round_data.append(int(split_data[0]))
#    price_data.append(float(split_data[1]))

#max_price = max(price_data)
#max_round = max(round_data)

#source = pd.DataFrame({
#    'price': price_data,
#    'trades': round_data
#})

x1 = [1,2,3,4,5,6,7,8,9,10]
y1 = [45.60,45.24,45.46,45.60,45.66,45.78,45.85,45.80,45.88,45.91]
plt.plot(x1, y1, label = "price")
plt.xlabel("trading round")
plt.ylabel("price")
plt.title("Stock")
plt.legend()
plt.show()

#chart = alt.Chart(source).mark_line().encode(
#    alt.X('rounds:Q',
#          scale=alt.Scale(domain=(min(round_data), max(round_data)), type='ordinal')),
#    alt.Y('price:Q',
#          scale=alt.Scale(domain=(0, max_price),))
#).properties(
#    width=1000,
#    height=800
#)

#filename = "chart_good_" + good_id + ".html"
#chart.serve()
#chart.save("chart.html")
