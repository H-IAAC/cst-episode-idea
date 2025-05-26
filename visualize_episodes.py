import argparse
import json
import matplotlib.pyplot as plt
from matplotlib.patches import Polygon
from matplotlib.lines import Line2D

colors = ['#e41a1c','#377eb8']

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

    entity_map = {}
    idx_count = 0

    idx_annotations = []
    for episode in idea['l']:
        object_name = episode['l'][0]['l'][0]['name']
        if object_name not in entity_map:
            entity_map[object_name] = {'idx': idx_count, 'count': 0, 'label': chr(idx_count + 65)} # ASCII code for 'A'==65
            idx_count += 1
        entity_map[object_name]['count'] += 1

        episode_idx = entity_map[object_name]['idx']
        episode_count = entity_map[object_name]['count']
        episode_label = entity_map[object_name]['label']

        if show_intermediate:
            px, py = None, None
            for situation in episode['l']:
                x, y = get_pos(situation)
                if px and py:
                    ax.annotate("", xy=(x,y), 
                                xytext=(px,py),
                                arrowprops=dict(
                                    arrowstyle="->",
                                    color=colors[episode_idx % len(colors)],
                                    linewidth=2,
                                ))
                if "Final" in situation['name']:
                    vx, vy = x-px, y-py
                    idx_annotations.append((str(episode_count) + episode_label, (x - 5*vx, y - 5*vy + 10)))

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

                ax.annotate("",
                            xy=(x2,y2),
                            xytext=(x1,y1),
                            arrowprops=dict(
                                arrowstyle="->",
                                color=colors[episode_idx % len(colors)],
                                linewidth=2,
                            ))

                vx, vy = x2-x1, y2-y1
                idx_annotations.append((str(episode_count) + episode_label, (x2 - 0.25*vx,y2 - 0.25*vy + 10)))

    for idx, pos in idx_annotations:
        ax.annotate(idx, xy=pos, fontsize=13, fontweight='bold')

    mock_handles = [Line2D([0], [0], color=colors[0], lw=4),
                    Line2D([0], [0], color=colors[1], lw=4)]
    ax.legend(mock_handles, ['Actor A', 'Actor B'], markerscale=3.0, fontsize=15)
    plt.show()
    return fig

if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog='visualize_episodes.py')

    parser.add_argument('file_path')
    parser.add_argument('--show-intermediate' , '-si', action='store_true')
    parser.add_argument('--output', '-o')

    args = parser.parse_args()

    fig = visualize_episode(args.file_path, args.show_intermediate)
    if args.output:
        fig.savefig(args.output, bbox_inches='tight')
