# -*- coding: utf-8 -*-
"""Goi trong `git filter-branch --index-filter`: doi moi duong dan cua index theo ban_do_gop.
    python3 -I loc_lich_su.py <repo>      (doc/ghi index qua GIT_INDEX_FILE)"""
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
sys.dont_write_bytecode = True
import ban_do_gop  # noqa: E402

repo = sys.argv[1]
idx = os.environ['GIT_INDEX_FILE']
ra = subprocess.run(['git', 'ls-files', '-s', '-z'], capture_output=True, check=True).stdout
moi = []
for muc in ra.split(b'\0'):
    if not muc:
        continue
    dau, duong = muc.split(b'\t', 1)
    moi.append(dau + b'\t' + ban_do_gop.doi(repo, duong.decode('utf-8', 'surrogateescape')).encode('utf-8', 'surrogateescape'))
env = dict(os.environ, GIT_INDEX_FILE=idx + '.moi')
if os.path.exists(idx + '.moi'):
    os.unlink(idx + '.moi')
subprocess.run(['git', 'update-index', '-z', '--index-info'], input=b'\0'.join(moi) + b'\0', env=env, check=True)
os.replace(idx + '.moi', idx)
