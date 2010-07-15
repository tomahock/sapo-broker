# coding=utf-8

from distutils.core import setup, Extension

version = '0.0.3'

setup(
    name='Broker',
    packages = ['SAPO'],
    package_dir = {'SAPO': 'src'},
    py_modules = ['SAPO.Broker', 'SAPO.iso8601'],
    author = 'Cláudio Valente',
    author_email = 'c.valente@co.sapo.pt',
    description="SAPO Broker python client",
    url="http://softwarelivre.sapo.pt/broker",
    version = version,
    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'Operating System :: OS Independent',
        'Programming Language :: Python :: 2.4',
        'Programming Language :: Python :: 2.5',
        'Programming Language :: Python :: 2.6',
        'Topic :: Software Development :: Libraries :: Python Modules',
    ],
)
