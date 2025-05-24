import argparse
import json
import matplotlib.pyplot as plt

colors = ['#e41a1c','#377eb8','#4daf4a','#984ea3','#ff7f00','#ffff33','#a65628','#f781bf','#999999']

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

def visualize_episode(file_path, show_intermediate):
    with open(file_path) as file:
        idea = json.load(file)

    for i, episode in enumerate(idea['l']):
        if show_intermediate:
            px, py = None, None
            for situation in episode['l']:
                x, y = get_pos(situation)
                if px and py:
                    plt.arrow(px,py,(x-px),(y-py), color=colors[i % len(colors)], head_width=5, head_length=2)
                px, py = x, y
        else:
            initial_sit, final_sit = None, None
            for situation in episode['l']:
                if "Initial" in situation['name']:
                    initial_sit = situation
                elif "Final" in situation['name']:
                    final_sit = situation

            if initial_sit and final_sit:
                x1, y1 = get_pos(initial_sit)
                x2, y2 = get_pos(final_sit)
                plt.arrow(x1,y1,(x2 - x1),(y2 - y1), color=colors[i % len(colors)], head_width=5, head_length=2)

    plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog='visualize_episodes.py')

    parser.add_argument('file_path')
    parser.add_argument('--show-intermediate' , '-si', action='store_true')

    args = parser.parse_args()

    visualize_episode(args.file_path, args.show_intermediate)
