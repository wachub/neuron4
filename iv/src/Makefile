SHELL = /bin/bash

srcdir = .
top_srcdir = ..

prefix = /home/job/neuron/iv
exec_prefix = /home/job/neuron/iv/x86_64

bindir = ${exec_prefix}/bin
sbindir = ${exec_prefix}/sbin
libexecdir = ${exec_prefix}/libexec
datadir = ${prefix}/share
sysconfdir = ${prefix}/etc
sharedstatedir = ${prefix}/com
localstatedir = ${prefix}/var
libdir = ${exec_prefix}/lib
infodir = ${prefix}/share/info
mandir = ${prefix}/share/man
includedir = ${prefix}/include
oldincludedir = /usr/include

DESTDIR =

pkgdatadir = $(datadir)/iv
pkglibdir = $(libdir)/iv
pkgincludedir = $(includedir)/iv

top_builddir = ..



SUBDIRS = lib include bin
subdir = .

ACLOCAL_M4 = $(top_srcdir)/aclocal.m4

mkinstalldirs = $(SHELL) $(top_srcdir)/mkinstalldirs

Makefile: $(srcdir)/Makefile.in  $(top_builddir)/config.status
	cd $(top_builddir) \
	  && CONFIG_FILES=$(subdir)/$@ CONFIG_HEADERS= $(SHELL) ./config.status

# This directory's subdirectories are mostly independent; you can cd
# into them and run `make' without going through this Makefile.
# To change the values of `make' variables: instead of editing Makefiles,
# (1) if the variable is set in `config.status', edit `config.status'
#     (which will cause the Makefiles to be regenerated when you run `make');
# (2) otherwise, pass the desired values on the `make' command line.

all install clean mostlyclean uninstall:
	@topdir=`pwd`; \
	for dir in $(SUBDIRS); do \
	  echo "Making $@ in $$dir"; \
	  cd $$dir; \
	  $(MAKE) $@; \
	  cd $$topdir; \
	done

distclean:
	@topdir=`pwd`; \
	for dir in $(SUBDIRS); do \
	  echo "Making $@ in $$dir"; \
	  cd $$dir; \
	  $(MAKE) $@; \
	  cd $$topdir; \
	done
	rm -f Makefile

.PHONY: all install clean mostlyclean distclean uninstall

# Tell versions [3.59,3.63) of GNU make to not export all variables.
# Otherwise a system limit (for SysV at least) may be exceeded.
.NOEXPORT:
