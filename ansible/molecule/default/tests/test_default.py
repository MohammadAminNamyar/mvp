# vim:set ts=4 sw=4 sts=4 et nowrap syntax=python:
#
def test_default(host):
    docker = host.service('docker')
    assert docker.is_running
    assert docker.is_enabled
