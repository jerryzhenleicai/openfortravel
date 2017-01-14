import lattice.tasks as tasks
depends = []
libs = ['commons', 'jgl']


def astar(mod, *args, **dict_p) :
    threads = "4"
    if dict_p.has_key("threads"):
        threads = dict_p['threads']
        del dict_p['threads']

    tasks.run(__name__, 'com.gaocan.train.route.AStarSearch', *['/var/opt/traindata  '  , threads],  **dict_p )
