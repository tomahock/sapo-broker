package SAPO::Broker::Utils;

use base qw{Exporter};
use Carp qw(croak carp);
use strict;
use warnings;

our @EXPORT_OK = qw{
    &class
    };

my $caller_pkg;

sub parent_sub($&) {
    my ( $name, $sub ) = @_;
    no strict 'refs';    ## no critic(ProhibitNoStrict) no other way around this
    *{ $caller_pkg . '::' . $name } = $sub;
    return;
}

#create simple classes on the fly
##try to keep dependencies to a minimum
sub class {
    my (%params) = @_;

    #XXX non reentrant
    $caller_pkg = caller(0);

    my @mandatory = @{ $params{'mandatory'} || [] };
    my @optional  = @{ $params{'optional'}  || [] };

    #aux function to create functions in the caller namespace

    #try to get carp and croak to report actual package
    local $Carp::CarpLevel = $Carp::CarpLevel + 1;    ## no critic

    #generate the constructor

    parent_sub 'new', sub {
        my ( $class, @params ) = @_;

        my %params;
        my $first_param = $params[0];

        if ( @params and UNIVERSAL::isa( $first_param, 'HASH' ) ) {
            %params = %$first_param;
        } else {
            %params = @params;
        }

        my $self = {};

        #check mandatory parameters
        for my $param (@mandatory) {
            if ( exists( $params{$param} ) ) {
                $self->{$param} = $params{$param};
            } else {
                croak "Missing mandatory parameter $param";
            }
        }

        #copy optional parameters
        for my $param (@optional) {
            if ( exists( $params{$param} ) ) {
                $self->{$param} = $params{$param};
            }
        }

        return bless $self, $class;
    };

    #generate getters and setters
    for my $param ( @mandatory, @optional ) {
        parent_sub $param, sub {
            my ( $self, $value ) = @_;
            if ( @_ > 1 ) {
                $self->{$param} = $value;
                return $self;
            } else {
                return $self->{$param};
            }
        };
    }

    return;
} ## end sub class

1;
