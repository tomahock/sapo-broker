# coding=utf-8

from distutils.core import setup, Extension

version = '0.0.1'

setup(
	name='Broker',
    packages = ['SAPO'],
    package_dir = {'SAPO': 'src'},
	py_modules = ['SAPO.Broker', 'SAPO.iso8601'],
	author = 'Cláudio Valente',
	author_email = 'c.valente@co.sapo.pt',
    description="SAPO Broker python client",
    url="http://softwarelivre.sapo.pt/broker",
	version = version
)
