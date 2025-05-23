import json
import matplotlib.pyplot as plt

colors = ['b','g','r','c','m','y','k']


def get_pos(situation):
    for property in situation['l'][0]['l']:
        if property['name'] == "Position":
            for quality in property['l']:
                if quality['name'] == "X":
                    x = quality['value'].replace(",",".")
                if quality['name'] == "Y":
                    y = quality['value'].replace(",",".")
            return float(x),float(y)
    return 0,0

with open("SimpleEpisodes") as file:
    idea = json.load(file)

for i, episode in enumerate(idea['l']):
    initial_sit, final_sit = None, None
    for situation in episode['l']:
        if "Initial" in situation['name']:
            initial_sit = situation
        elif  "Final" in situation['name']:
            final_sit = situation

    if initial_sit and final_sit:
        x1, y1 = get_pos(initial_sit)
        x2, y2 = get_pos(final_sit)

        plt.arrow(x1,y2,(x2-x1),(y2-y1), color=colors[i], head_width=5, head_length=2)
plt.show()
