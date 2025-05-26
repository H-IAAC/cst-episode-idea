import math
import argparse
import json
import matplotlib.pyplot as plt
from matplotlib.patches import Polygon

colors_1 = ['#a6cee3','#1f78b4','#b2df8a','#33a02c','#fb9a99','#e31a1c','#fdbf6f','#ff7f00','#cab2d6','#6a3d9a','#ffff99']
colors_2 = ['#e41a1c','#377eb8','#4daf4a','#984ea3','#ff7f00','#a65628','#f781bf','#999999']

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

def visualize_episode(file_path, show_intermediate, colors):
    fig, ax = plt.subplots(figsize=(10,10))
    ax.set_xlim([150,650])
    ax.set_ylim([50,600])
    ax.tick_params(axis='both', which='major', labelsize=13)

    agent_shape = [[380, 590], [420,590], [400,590-34]]
    agent = Polygon(agent_shape, fc='tab:green')
    ax.add_patch(agent)
    ax.annotate("Agent", xy=(430, 570), fontsize=14, fontweight='bold')

    with open(file_path) as file:
        idea = json.load(file)

    idx_annotations = []
    for i, episode in enumerate(idea['l']):
        if show_intermediate:
            px, py = None, None
            for situation in episode['l']:
                x, y = get_pos(situation)
                if px and py:
                    ax.annotate("", xy=(x,y), 
                                xytext=(px,py),
                                arrowprops=dict(
                                    arrowstyle="->",
                                    color=colors[i % len(colors)],
                                    linewidth=2,
                                ))
                px, py = x, y
                if "Final" in situation['name']:
                    idx_annotations.append((str(i+1), (x,y)))
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
                ax.annotate("",
                            xy=(x2,y2),
                            xytext=(x1,y1),
                            arrowprops=dict(
                                arrowstyle="->",
                                color=colors[i % len(colors)],
                                linewidth=2,
                            ))
                idx_annotations.append((str(i+1), (x2,y2)))

    for idx, pos in idx_annotations:
        ax.annotate(idx, xy=pos, fontsize=12, fontweight='bold')

    plt.show()
    return fig

if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog='visualize_episodes.py')

    parser.add_argument('file_path')
    parser.add_argument('--show-intermediate' , '-si', action='store_true')
    parser.add_argument('--output', '-o')

    args = parser.parse_args()

    fig = visualize_episode(args.file_path, args.show_intermediate, colors_2)
    if args.output:
        fig.savefig(args.output, bbox_inches='tight')
